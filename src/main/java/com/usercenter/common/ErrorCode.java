package com.usercenter.common;

/**
 * 错误码枚举类
 *
 * @author humeng
 */
public enum ErrorCode {

    PARAMS_ERROR(40000, "参数不合法", ""),
    LOGIN_ERROR(40005, "登陆失败", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN_ERROR(40100, "未登录", ""),
    NO_AUTH_ERROR(40101, "无权限", ""),
    ACCOUNT_REPEAT(40201, "账户重复", ""),

    ACCOUNT_STATUS_DISABLE(40202, "账户被禁用", ""),
    SERVICE_ERROR(50000, "操作失败", ""),

    SYSTEM_ERROR(50001, "", "系统内部异常");

    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
