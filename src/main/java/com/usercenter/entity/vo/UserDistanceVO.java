package com.usercenter.entity.vo;


import lombok.Data;

/**
 * 用户信息 脱敏
 */
@Data
public class UserDistanceVO {

    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    private Double distance;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     *
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 标签列表JSON
     */
    private String tags;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 是否有效,0:正常
     */
    private Integer userStatus;

    /**
     * 用户权限;0:普通用户;1:管理员
     */
    private Integer userRole;
}
