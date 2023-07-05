package com.usercenter.entity.vo;

import lombok.Data;

/**
 * 队伍成员封装类
 */
@Data
public class TeamMemberVO {
    /**
     * id
     */
    private Long id;


    /**
     * 队伍名称
     */
    private String name;


    /**
     * 用户id-队长
     */
    private Long userId;

    /**
     * 成员ID
     */
    private Long memberId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     *
     */
    private Integer gender;


    /**
     * 标签列表JSON
     */
    private String tags;


}
