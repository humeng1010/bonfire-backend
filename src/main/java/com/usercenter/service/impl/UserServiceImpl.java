package com.usercenter.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.constant.RedisConstant;
import com.usercenter.entity.User;
import com.usercenter.exception.BusinessException;
import com.usercenter.mapper.UserMapper;
import com.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.usercenter.common.ErrorCode.*;
import static com.usercenter.constant.UserConstant.*;

/**
 * 用户服务实现类
 *
 * @author humeng
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-06-10 13:51:14
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public BaseResponse<Long> userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8
                || checkPassword.length() < 8
                || userPassword.length() > 18
                || checkPassword.length() > 18) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }

        // 4.密码
        if (!Objects.equals(userPassword, checkPassword)) {
            return BaseResponse.error(ErrorCode.PARAMS_ERROR);
        }
        // TODO 方便测试环境
        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        // if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
        //     return BaseResponse.error(50006, "密码过于简单");
        // }

        // 2.账户不能重复
        // 2.1查数据库
        Long count = this.query().eq("userAccount", userAccount).count();
        if (count > 0) {
            // 被注册
            // return BaseResponse.error(ACCOUNT_REPEAT);
            throw new BusinessException(ACCOUNT_REPEAT);
        }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        // 插入数据
        User user = new User();
        user.setUsername("momo_" + IdUtil.nanoId(5));
        user.setAvatarUrl("https://img.alicdn.com/bao/uploaded/i1/232692832/O1CN01XERLVq1Wn6Sq5ufB4_!!232692832.jpg_400x400q90");
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        this.save(user);
        return BaseResponse.ok(user.getId());
    }

    @Override
    public BaseResponse<User> doLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return BaseResponse.error(PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            return null;
        }
        // 1.1密码长度8-18位
        if (userPassword.length() < 8 || userPassword.length() > 18) {
            return BaseResponse.error(PARAMS_ERROR);
        }

        // 3.账户
        // 匹配特殊字符一次或多次
        if (userAccount.matches("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\\\]+")) {
            return BaseResponse.error(PARAMS_ERROR);
        }

        // TODO 方便测试环境
        // 4.2 密码必须包含至少一个数字、一个小写字母、一个大写字母和一个特殊字符,密码长度必须在 8 到 18 个字符之间。
        // if (!userPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[~`!@#$%^&*()-_+=\\\\[\\\\]\\\\{}|;:',.<>/?]).{8,18}$")) {
        //     return BaseResponse.error(50014, "密码过于简单");
        // }

        // 对密码进行加盐加密
        String encryptPassword = SaSecureUtil.md5(SALT + userPassword);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User databaseUser = this.getOne(queryWrapper);

        if (Objects.isNull(databaseUser)) {
            // 用户不存在
            log.info("user login failed, userAccount can not match userPassword");
            // return BaseResponse.error(LOGIN_ERROR);
            throw new BusinessException(LOGIN_ERROR);
        }
        if (Objects.equals(databaseUser.getUserStatus(), 1)) {
            //    用户被禁用
            throw new BusinessException(ACCOUNT_STATUS_DISABLE);

        }

        // 用户脱敏
        User safetyUser = getSafetyUser(databaseUser);

        // 记录用户状态 session
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);

        return BaseResponse.ok(safetyUser, "登录成功");
    }

    @Override
    public User getSafetyUser(User loginUser) {
        User safetyUser = new User();
        BeanUtils.copyProperties(loginUser, safetyUser, "userPassword", "updateTime", "isDelete");
        return safetyUser;
    }

    @Override
    public BaseResponse<IPage<User>> searchUsers(Long current, Long pageSize, String username) {
        IPage<User> userPage = new Page<>(current, pageSize);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), User::getUsername, username);

        this.page(userPage, queryWrapper);

        userPage.setRecords(userPage.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList()));

        return BaseResponse.ok(userPage);
    }

    /**
     * 分页根据标签查询用户
     *
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @param tagNameList 标签列表
     * @return 符合标签的分页数据
     */
    @Override
    public BaseResponse<IPage<User>> searchUsersByTags(Long currentPage, Long pageSize, List<String> tagNameList) {
        User currentLoginUser = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        if (currentLoginUser == null) {
            return BaseResponse.error(NOT_LOGIN_ERROR);
        }
        // 判断标签集合不能为空
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(NULL_ERROR);
        }
        IPage<User> userPage = new Page<>(currentPage, pageSize);

        // 2. 通过内存查询 更灵活
        // 2.1 查询当前页所有的用户
        this.page(userPage);
        // 2.2 获取当前页的用户集合
        List<User> userList = userPage.getRecords();
        Gson gson = new Gson();

        // 过滤不满足条件的用户
        List<User> users = userList.stream().filter(user -> {
                    // 获取用户标签JSON
                    String tags = user.getTags();
                    if (StrUtil.isBlank(tags)) {
                        return false;
                    }
                    // 把JSON数组转为Set集合
                    Set<String> tagSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
                    }.getType());

                    // 遍历查询标签集合
                    for (String tagName : tagNameList) {
                        // 如果用户标签不包含查询的标签则过滤掉
                        if (tagSet.contains(tagName)) {
                            return true;
                        }
                    }
                    // 如果遍历完了都没有返回false则说明该用户满足条件
                    return false;
                }).map(this::getSafetyUser)
                .collect(Collectors.toList());

        // 删除集合中的自己
        users.removeIf(user -> Objects.equals(user.getId(), currentLoginUser.getId()));
        // 更新结果并返回
        userPage.setRecords(users);

        return BaseResponse.ok(userPage, "根据标签查询用户成功");
    }

    /**
     * 根据标签查询所有用户
     *
     * @param tags 标签
     * @return 所有符合标签的用户
     */
    @Override
    public BaseResponse<List<User>> searchAllUsersByTags(List<String> tags) {
        List<User> list = this.list();

        List<User> result = list.stream().filter(user -> {
            String tagListStr = user.getTags();
            Gson gson = new Gson();
            Set<String> tagSet = gson.fromJson(tagListStr, new TypeToken<Set<String>>() {
            }.getType());
            // 简化版的非空处理 如果set集合为空则会执行orElse
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());

            // 全部转换为大写再进行比较
            tagSet = tagSet.stream().map(String::toUpperCase).collect(Collectors.toSet());

            for (String tag : tags) {
                // 如果条件不在数据库的集合中
                if (tagSet.contains(tag.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }).map(this::getSafetyUser).collect(Collectors.toList());

        return BaseResponse.ok(result);
    }

    @Override
    public BaseResponse<String> updateUser(User user) {
        // 仅管理员和自己可以修改
        User cacheUser = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        if (cacheUser == null) {
            throw new BusinessException(NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        Long cacheUserId = cacheUser.getId();

        if (Objects.equals(cacheUser.getUserRole(), ADMIN_ROLE)) {
            //    是管理员,直接可以修改所有用户的信息
            this.updateById(user);
            return BaseResponse.ok("ok", "修改成功");
        }
        if (!Objects.equals(cacheUserId, userId)) {
            // 如果普通用户修改的不是自己的信息,则抛出异常
            throw new BusinessException(NO_AUTH_ERROR);
        }
        this.updateById(user);

        return BaseResponse.ok("ok", "修改成功");
    }

    @Override
    public BaseResponse<IPage<User>> recommendUsers(Long currentPage, Long pageSize) {

        User currentLoginUser = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATUS);
        if (currentLoginUser == null) {
            String key = RedisConstant.USER_RECOMMEND_KEY + currentPage + "-" + pageSize;
            // 读取redis
            String pageStr = stringRedisTemplate.opsForValue()
                    .get(key);
            Gson gson = new Gson();
            Page cacheResponse = JSONUtil.toBean(pageStr, Page.class);
            if (BeanUtil.isNotEmpty(cacheResponse)) {
                return BaseResponse.ok(cacheResponse, "默认推荐成功");
            }
            // redis没有,则读取数据库然后保存到redis
            IPage<User> userPage = new Page<>(currentPage, pageSize);
            //    如果没有登录则直接返回10条数据
            this.page(userPage);
            // 缓存到redis
            String userPageJson = gson.toJson(userPage);
            try {
                stringRedisTemplate.opsForValue().set(key, userPageJson);
            } catch (Exception e) {
                log.error("redis缓存失败");
            }
            return BaseResponse.ok(userPage, "默认推荐成功");
        }

        // 读取缓存
        String key = RedisConstant.USER_RECOMMEND_KEY + currentLoginUser.getId() + ":" + currentPage + "-" + pageSize;

        String resultStr = stringRedisTemplate.opsForValue().get(key);
        BaseResponse cacheResponse = JSONUtil.toBean(resultStr, BaseResponse.class);

        if (BeanUtil.isNotEmpty(cacheResponse)) {
            return cacheResponse;
        }


        // 缓存没有,读数据库,然后写缓存
        // 用户登录了,根据用户的标签推荐相同标签的
        String tags = currentLoginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());

        BaseResponse<IPage<User>> result = this.searchUsersByTags(currentPage, pageSize, tagList);
        String resultStrForRedis = gson.toJson(result);
        try {
            stringRedisTemplate.opsForValue().set(key, resultStrForRedis, RedisConstant.USER_RECOMMEND_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis缓存失败");
        }
        return result;
    }

    /**
     * 通过SQL 根据标签查询用户 废弃的方法
     *
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @param tagNameList 标签列表
     * @return 符合标签的分页数据
     */
    @Deprecated
    private BaseResponse<IPage<User>> searchUsersByTagsBySQL(Long currentPage, Long pageSize, List<String> tagNameList) {
        if (CollectionUtil.isEmpty(tagNameList)) {
            throw new BusinessException(NULL_ERROR);
        }
        IPage<User> userPage = new Page<>(currentPage, pageSize);
        // 1. 通过数据库查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        this.page(userPage, queryWrapper);

        userPage.setRecords(userPage.getRecords()
                .stream().map(this::getSafetyUser).collect(Collectors.toList()));


        return BaseResponse.ok(userPage, "根据标签查询用户成功");
    }


}




