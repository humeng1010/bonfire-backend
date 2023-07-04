package com.usercenter.constant;


/**
 * 用户常量接口
 *
 * @author humeng
 */
public interface UserConstant {
    /**
     * 用户登录状态
     * session key
     */
    String USER_LOGIN_STATUS = "userLoginStatus";

    /**
     * 密码加密的盐
     * md5算法
     */
    String SALT = "salt";

    /**
     * 普通用户
     */
    Integer DEFAULT_ROLE = 0;

    /**
     * 管理员用户
     */
    Integer ADMIN_ROLE = 1;

    
}
