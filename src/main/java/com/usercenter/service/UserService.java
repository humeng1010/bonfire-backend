package com.usercenter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usercenter.common.BaseResponse;
import com.usercenter.entity.User;
import com.usercenter.entity.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author humeng
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-06-10 13:51:14
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户ID
     */
    BaseResponse<Long> userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 登陆
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 返回脱敏后的用户信息
     */
    BaseResponse<User> doLogin(String userAccount, String userPassword);

    /**
     * 获取脱敏的用户数据
     *
     * @param loginUser 源用户数据,包含敏感信息
     * @return 不含敏感信息的用户数据
     */
    User getSafetyUser(User loginUser);

    /**
     * 根据用户名搜索用户
     *
     * @param current
     * @param pageSize
     * @param username 用户名
     * @return 所有匹配的用户列表
     */
    BaseResponse<IPage<User>> searchUsers(Long current, Long pageSize, String username);

    /**
     * 根据标签搜索用户
     *
     * @param currentPage
     * @param pageSize
     * @param tagNameList 标签列表
     * @return 分页结果
     */
    BaseResponse<IPage<User>> searchUsersByTags(Long currentPage, Long pageSize, List<String> tagNameList);

    BaseResponse<List<User>> searchAllUsersByTags(List<String> tags);

    BaseResponse<String> updateUser(User user);


    BaseResponse<IPage<User>> recommendUsers(Long currentPage, Long pageSize);

    User getLoginUser(HttpServletRequest request);

    BaseResponse<List<UserVO>> matchUser(Integer num, User loginUser);
}
