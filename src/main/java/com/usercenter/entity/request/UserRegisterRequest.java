package com.usercenter.entity.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注册请求体
 *
 * @author humeng
 */
@Data
@ApiModel(description = "注册请求数据", value = "用户账户和密码以及确认密码")
public class UserRegisterRequest {

    @ApiModelProperty(name = "userAccount", value = "账户", required = true)
    private String userAccount;

    @ApiModelProperty(name = "userPassword", value = "密码", required = true)
    private String userPassword;

    @ApiModelProperty(name = "checkPassword", value = "确认密码", required = true)
    private String checkPassword;
}
