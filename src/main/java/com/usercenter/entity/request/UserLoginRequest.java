package com.usercenter.entity.request;


import lombok.Data;

/**
 * 用户登陆请求体
 *
 * @author humeng
 */
@Data
public class UserLoginRequest {

    private String userAccount;

    private String userPassword;

}
