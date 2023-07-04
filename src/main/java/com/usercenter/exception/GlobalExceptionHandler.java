package com.usercenter.exception;

import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 *
 * @author humeng
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常拦截器
     *
     * @param businessException 业务异常
     * @return 响应信息
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> businessExceptionHandler(BusinessException businessException) {
        return BaseResponse
                .error(businessException.getCode(),
                        businessException.getMessage(),
                        businessException.getDescription());
    }

    /**
     * 全局异常拦截器
     *
     * @param e 运行时异常
     * @return 响应信息
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> globalExceptionHandler(RuntimeException e) {
        return BaseResponse
                .error(ErrorCode.SYSTEM_ERROR, e.getMessage());

    }
}
