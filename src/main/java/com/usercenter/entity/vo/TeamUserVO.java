package com.usercenter.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 队伍信息封装类
 */
@Data
public class TeamUserVO {
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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 当前人数
     */
    private long currentNum;

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
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建人
     */
    UserVO createUser;


}
