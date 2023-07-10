package com.usercenter.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.Team;
import com.usercenter.entity.User;
import com.usercenter.entity.UserTeam;
import com.usercenter.entity.dto.TeamQuery;
import com.usercenter.entity.enums.TeamStatusEnum;
import com.usercenter.entity.request.TeamJoinRequest;
import com.usercenter.entity.request.TeamQuitRequest;
import com.usercenter.entity.request.TeamUpdateRequest;
import com.usercenter.entity.vo.TeamUserInfoVO;
import com.usercenter.entity.vo.TeamUserVO;
import com.usercenter.entity.vo.UserVO;
import com.usercenter.exception.BusinessException;
import com.usercenter.mapper.TeamMapper;
import com.usercenter.service.TeamService;
import com.usercenter.service.UserService;
import com.usercenter.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.usercenter.common.ErrorCode.PARAMS_ERROR;
import static com.usercenter.constant.RedisConstant.ADD_TEAM_KEY;
import static com.usercenter.constant.RedisConstant.JOIN_TEAM_LOCK;

/**
 * @author humeng
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-07-04 16:09:29
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;


    /**
     * 创建队伍
     *
     * @param team
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> addTeam(Team team, User user) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        String name = team.getName();
        if (StrUtil.isBlank(name) || StrUtil.length(name) > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        String avatarUrl = team.getAvatarUrl();
        if (StrUtil.isBlank(avatarUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍必须有头像");
        }
        String description = team.getDescription();
        if (StrUtil.length(description) > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);

        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        String password = team.getPassword();
        if (TeamStatusEnum.ENCRYPT.equals(statusEnum) && (StrUtil.isBlank(password) || password.length() != 6 || !NumberUtil.isNumber(password))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        Date expireTime = team.getExpireTime();

        // 如果当前时间在过期时间之后 说明已经过期了
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间大于当前时间");
        }

        // 如果用户疯狂点击count刚开始是1 多个线程同时进来发现count都是1 都会通过,则会出现超过5个队伍的情况,解决方案使用redisson分布式锁
        RLock lock = redissonClient.getLock(ADD_TEAM_KEY + user.getId());
        try {
            // 具有自动续期机制
            lock.lock();

            LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
            Long userId = user.getId();
            queryWrapper.eq(userId != null && userId != 0, Team::getUserId, userId);
            long count = this.count(queryWrapper);
            if (count >= 5) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍创建上限");
            }
            //     8.插入队伍信息
            team.setId(null);
            team.setUserId(userId);
            boolean save = this.save(team);
            if (!save) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
            }
            // if (true) {
            //     throw new BusinessException(ErrorCode.SYSTEM_ERROR, "测试事务");
            // }
            //    9.插入用户和队伍关系到关系表
            UserTeam userTeam = new UserTeam();
            userTeam.setUserid(userId);
            Long teamId = team.getId();
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            boolean save1 = userTeamService.save(userTeam);
            if (!save1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建用户-队伍失败");
            }
            return BaseResponse.ok(teamId);

        } finally {
            lock.unlock();
        }

    }

    @Override
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, User loginUser) {
        // 1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            queryWrapper.eq(id != null && id > 0, Team::getId, id);

            String name = teamQuery.getName();
            queryWrapper.like(StrUtil.isNotBlank(name), Team::getName, name);

            String description = teamQuery.getDescription();
            queryWrapper.like(StrUtil.isNotBlank(description), Team::getDescription, description);

            String searchText = teamQuery.getSearchText();
            queryWrapper.like(StrUtil.isNotBlank(searchText), Team::getName, searchText)
                    .like(StrUtil.isNotBlank(searchText), Team::getDescription, searchText);

            Integer maxNum = teamQuery.getMaxNum();
            queryWrapper
                    .eq(maxNum != null && maxNum > 0 && maxNum <= 20, Team::getMaxNum, maxNum);

            Long userId = teamQuery.getUserId();
            queryWrapper.eq(userId != null && userId > 0, Team::getUserId, userId);

            Integer userRole = loginUser.getUserRole();
            Integer status = teamQuery.getStatus();
            TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);

            if (!Objects.equals(userRole, 1) && TeamStatusEnum.PRIVATE.equals(enumByValue)) {
                // 如果不是管理员,并且查看的是私有的
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 如果指定了查询条件,则拼接条件
            if (enumByValue != null) {
                queryWrapper.eq(Team::getStatus, enumByValue.getValue());
            } else {
                // 如果没有指定查询条件,则默认查询公开和加密的
                queryWrapper.ne(Team::getStatus, TeamStatusEnum.PRIVATE.getValue());

            }
        }
        queryWrapper.orderByDesc(Team::getCreateTime);

        // 查询的所有队伍信息,包括已经过期的队伍
        List<Team> teamList = this.list(queryWrapper);
        if (teamList.isEmpty()) {
            return BaseResponse.ok(Collections.emptyList());
        }
        // 2. 不展示已过期的队伍（根据过期时间筛选）
        List<Team> effectiveTeams = teamList.stream().filter(team -> team.getExpireTime() == null || new Date().before(team.getExpireTime())).collect(Collectors.toList());
        // 关联查询创建人信息
        List<TeamUserVO> teamUserVOS = new ArrayList<>();
        for (Team effectiveTeam : effectiveTeams) {
            Long userId = effectiveTeam.getUserId();
            if (userId == null) continue;
            User user = userService.getById(userId);
            // 把完整用户信息脱敏
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            // 把有效的队伍信息脱敏
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(effectiveTeam, teamUserVO);
            // 给viewObject设置创建者
            teamUserVO.setCreateUser(userVO);
            long teamCurrentCountById = this.getTeamCurrentCountById(effectiveTeam.getId());
            teamUserVO.setCurrentNum(teamCurrentCountById);

            teamUserVOS.add(teamUserVO);

        }
        return BaseResponse.ok(teamUserVOS, "查询队伍和创建人信息成功");
    }

    @Override
    public BaseResponse<Boolean> updateTeam(TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 用户自身可以修改,管理员也可以修改
        // 不是当前用户不可修改,不是管理员也不可以修改
        if (!Objects.equals(oldTeam.getUserId(), loginUser.getId()) && !Objects.equals(loginUser.getUserRole(), 1)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 判断是否修改了

        // 更新
        Integer status = teamUpdateRequest.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态不存在");
        }
        //    如果是加密的,就要设置密码,
        if (Objects.equals(enumByValue, TeamStatusEnum.ENCRYPT)) {
            //    先从数据库取出密码,看是否已经设置过了
            LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Team::getId, id);
            Team team = this.getOne(queryWrapper);
            String password = team.getPassword();
            //    如果没有设置,看这次的请求参数中是否有密码
            if (StrUtil.isBlank(password)) {
                String newPassword = teamUpdateRequest.getPassword();
                //    如果没有密码,则抛出异常
                if (StrUtil.isBlank(newPassword)) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密的房间必须要设置密码");
                }
            }
        } else {
            //    不是加密的清空该队伍的密码
            LambdaUpdateWrapper<Team> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Team::getPassword, null).eq(Team::getId, id);
            this.update(updateWrapper);
        }

        //    如果是公开或者私有的,则不允许设置密码
        if (Objects.equals(enumByValue, TeamStatusEnum.PUBLIC) || Objects.equals(enumByValue, TeamStatusEnum.PRIVATE)) {
            String newPassword = teamUpdateRequest.getPassword();
            if (StrUtil.isNotBlank(newPassword)) {
                // 如果设置了密码,则抛出异常
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非加密的房间不能设置密码");
            }
        }
        // Team team = new Team();
        // BeanUtils.copyProperties(teamUpdateRequest, team);
        LambdaUpdateWrapper<Team> updateWrapper = new LambdaUpdateWrapper<>();

        String name = teamUpdateRequest.getName();
        String avatarUrl = teamUpdateRequest.getAvatarUrl();
        String description = teamUpdateRequest.getDescription();
        Integer maxNum = teamUpdateRequest.getMaxNum();
        String password = teamUpdateRequest.getPassword();
        Date expireTime = teamUpdateRequest.getExpireTime();
        updateWrapper.set(StrUtil.isNotBlank(name), Team::getName, name)
                .set(StrUtil.isNotBlank(avatarUrl), Team::getAvatarUrl, avatarUrl)
                .set(StrUtil.isNotBlank(description), Team::getDescription, description)
                .set(maxNum != null && maxNum > 1 && maxNum < 20, Team::getMaxNum, maxNum)
                .set(enumByValue != null, Team::getStatus, enumByValue.getValue())
                .set(StrUtil.isNotBlank(password), Team::getPassword, password)
                .set(expireTime != null && new Date().before(expireTime), Team::getExpireTime, expireTime)
                .eq(Team::getId, id);
        boolean update = this.update(updateWrapper);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return BaseResponse.ok(true);
    }

    @Override
    public BaseResponse<Boolean> joinTeam(TeamJoinRequest teamJoinRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long userId = loginUser.getId();

        // 2. 队伍必须存在，只能加入未满、未过期的队伍
        Long teamId = teamJoinRequest.getTeamId();
        Team team = this.getById(teamId);

        if (team == null) {
            throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "队伍不存在");
        }

        // 3.禁止加入过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            //    当前时间大于过期时间
            throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "队伍已过期");
        }
        // 4.禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (Objects.equals(enumByValue, TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "禁止加入私有队伍");
        }
        // 5.如果是加密的,密码必须匹配
        if (Objects.equals(enumByValue, TeamStatusEnum.ENCRYPT)) {
            String password = teamJoinRequest.getPassword();
            if (StrUtil.isBlank(password) || !Objects.equals(password, team.getPassword())) {
                throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "密码不正确");
            }
        }

        // 考虑加分布式锁防止用户加入超过5个有效的队伍 考虑查询的优化先后顺序
        //  TODO 控制加锁的粒度
        RLock lock = redissonClient.getLock(JOIN_TEAM_LOCK);

        try {
            lock.lock();
            // 1. 用户最多加入 5 个队伍
            // 根据userId查询关系表中该用户加入了几个有效的队伍
            // 先查出有几个队伍,获取队伍ID,再根据队伍ID查未过期时间的有几个
            LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserTeam::getUserid, userId);
            List<UserTeam> userTeams = userTeamService.list(queryWrapper);
            List<Long> teamIds = userTeams.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
            if (!teamIds.isEmpty()) {
                // 根据队伍ID查未过期时间的有几个
                LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                teamLambdaQueryWrapper.in(Team::getId, teamIds).lt(Team::getExpireTime, new Date());
                long count = this.count(teamLambdaQueryWrapper);
                if (count > 5) {
                    throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX);
                }
            }
            // 6.禁止加入满员的队伍
            Integer maxNum = team.getMaxNum();
            // 获取当前队伍有多少人(多少记录)
            long currentNum = getTeamCurrentCountById(teamId);
            if (currentNum >= maxNum) {
                throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "队伍已满");
            }

            // 7. 不能重复加入已加入的队伍（幂等性）
            LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserTeam::getUserid, userId)
                    .eq(UserTeam::getTeamId, teamId);
            long isJoined = userTeamService.count(lambdaQueryWrapper);
            if (isJoined > 0) {
                throw new BusinessException(ErrorCode.TEAM_COUNT_OVER_MAX, "你已加入了该队伍,请勿重复加入");
            }


            // 6. 新增队伍 - 用户关联信息
            UserTeam userTeam = new UserTeam();
            userTeam.setUserid(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());

            return BaseResponse.ok(userTeamService.save(userTeam));
        } finally {
            lock.unlock();
        }

    }

    /**
     * 根据队伍id获取当前队伍有多少人
     *
     * @param teamId
     * @return
     */
    public long getTeamCurrentCountById(Long teamId) {
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        return userTeamService.count(userTeamLambdaQueryWrapper);
    }

    /**
     * 当前用户退出队伍
     *
     * @param teamQuitRequest    请求体 包含了队伍的id
     * @param httpServletRequest request
     * @return 是否退出成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> quitTeam(TeamQuitRequest teamQuitRequest, HttpServletRequest httpServletRequest) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String message = null;
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long userId = loginUser.getId();

        Long teamId = teamQuitRequest.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = this.getById(teamId);
        // 判断要退出的队伍是否存在,不存在抛出异常
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        // 判断当前用户有没有在该队伍中,否则抛出异常
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId)
                .eq(UserTeam::getUserid, userId);
        long count = userTeamService.count(queryWrapper);
        if (count < 1) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "您已经不在该队伍中");
        }

        // 判断当前队伍还有几个人
        long teamCurrentCount = getTeamCurrentCountById(teamId);
        // 如果只有1人,则解散队伍,删除队伍和关系
        if (teamCurrentCount == 1) {
            // 删除队伍
            this.removeById(teamId);
            // 根据队伍id删除用户和队伍关系数据
            userTeamService.update().eq("teamId", teamId).remove();
            message = "您已解散队伍";
        } else {
            // 如果队伍不是1人
            // 获取该队伍的队长id
            Long captainId = team.getUserId();
            if (!Objects.equals(userId, captainId)) {
                //    不是队长,直接退出:删除该用户-队伍关系表数据;
                userTeamService.update().eq("userId", userId).eq("teamId", teamId).remove();
                message = "您已退出队伍";
            } else {
                //    是队长,而且当前队伍人数大于1
                //    先查询关系表中该队伍的成员
                LambdaQueryWrapper<UserTeam> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                teamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
                List<UserTeam> teamMembers = userTeamService.list(teamLambdaQueryWrapper);
                // 根据加入的时间排序,获取到第二个用户的ID
                teamMembers.sort((o1, o2) -> DateUtil.compare(o1.getJoinTime(), o2.getJoinTime()));
                if (teamMembers.isEmpty() || teamMembers.size() < 2) {
                    throw new BusinessException(ErrorCode.NULL_ERROR, "无法操作quit-team:440");
                }
                UserTeam userTeam = teamMembers.get(1);
                Long secondJoinTeamUserId = userTeam.getUserid();

                //    修改队伍表的userId为该用户的ID
                this.update().set("userId", secondJoinTeamUserId).eq("id", teamId).update();

                //    队长退出队伍
                userTeamService.update().eq("userId", userId).eq("teamId", teamId).remove();

                message = "您已转让队伍";
            }
        }


        return BaseResponse.ok(true, message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId) {
        // 1. 校验请求参数
        if (teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        // 3. 校验你是不是队伍的队长
        Long userId = team.getUserId();
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (!Objects.equals(userId, loginUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 4. 移除所有加入队伍的关联信息
        // 根据队伍ID删除关系表的数据
        boolean result = userTeamService.update().eq("teamId", teamId).remove();
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }

        // 5. 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 根据当前登录的用户获取加入的队伍信息和成员信息
     *
     * @param id userID
     * @return BaseResponse
     */
    @Override
    public BaseResponse<List<TeamUserInfoVO>> getTeamByCurrentUserId(Long id) {
        if (id == null) {
            throw new BusinessException(PARAMS_ERROR);
        }
        // 1. 在关系表中获取当前用户加入的队伍关系：根据userId查找数据
        List<UserTeam> userTeams = userTeamService.lambdaQuery().eq(UserTeam::getUserid, id).list();
        List<Long> teamIds = userTeams.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        // 2. 获取该teamId，从team表中获取该队伍的信息
        List<Team> teams = this.listByIds(teamIds);
        List<TeamUserInfoVO> teamUserInfoVOS = new ArrayList<>();
        for (Team team : teams) {
            TeamUserInfoVO teamUserInfoVO = new TeamUserInfoVO();
            BeanUtils.copyProperties(team, teamUserInfoVO);

            long teamCurrentCountById = this.getTeamCurrentCountById(team.getId());
            teamUserInfoVO.setCurrentNum((int) teamCurrentCountById);

            // 3. 在关系表中再根据teamId查找数据
            List<UserTeam> userTeams1 = userTeamService.lambdaQuery().eq(UserTeam::getTeamId, team.getId()).list();
            ArrayList<UserVO> members = new ArrayList<>();

            for (UserTeam userTeam : userTeams1) {
                // 获取该team对应的userId,再查user表中对应的user信息
                User user = userService.getById(userTeam.getUserid());
                UserVO member = new UserVO();
                BeanUtils.copyProperties(user, member);
                members.add(member);
            }

            teamUserInfoVO.setMembers(members);

            teamUserInfoVOS.add(teamUserInfoVO);
        }
        teamUserInfoVOS.sort((o1, o2) -> DateUtil.compare(o2.getCreateTime(), o1.getCreateTime()));
        return BaseResponse.ok(teamUserInfoVOS);
    }
}




