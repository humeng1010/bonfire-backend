package com.usercenter.entity.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value = "添加队伍请求参数")
@Data
public class TeamAddRequest {

    /**
     * 队伍名称
     */
    @ApiModelProperty(value = "队伍名称")
    private String name;


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
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间,格式:yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 密码
     */
    private String password;

}
