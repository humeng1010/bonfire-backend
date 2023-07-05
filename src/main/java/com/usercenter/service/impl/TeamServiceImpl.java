package com.usercenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.Team;
import com.usercenter.entity.User;
import com.usercenter.entity.UserTeam;
import com.usercenter.entity.enums.TeamStatusEnum;
import com.usercenter.exception.BusinessException;
import com.usercenter.mapper.TeamMapper;
import com.usercenter.service.TeamService;
import com.usercenter.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

import static com.usercenter.constant.RedisConstant.ADD_TEAM_KEY;

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
        if (TeamStatusEnum.ENCRYPT.equals(statusEnum) && (StrUtil.isBlank(password) || password.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        Date expireTime = team.getExpireTime();
        // 如果当前时间在过期时间之后 说明已经过期了
        if (new Date().after(expireTime)) {
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
}




