package com.usercenter.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍-队伍用户信息 view object
 */
@Data
public class TeamUserInfoVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;
    /**
     * 队伍头像
     */
    private String avatarUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 当前人数
     */
    private Integer currentNum;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 队伍成员信息
     */
    private List<UserVO> members;

}