package com.usercenter.exception;

import com.usercenter.common.ErrorCode;

/**
 * 自定义异常类
 *
 * @author humeng
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String description;

    public BusinessException(Integer code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String desc) {
        super(desc);
        this.code = errorCode.getCode();
        this.description = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
