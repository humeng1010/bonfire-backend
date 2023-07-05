package com.usercenter.entity.request;

import lombok.Data;

import java.util.Date;

/**
 * 修改队伍参数信息
 *
 * @TableName team
 */
@Data
public class TeamUpdateRequest {

    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;


}