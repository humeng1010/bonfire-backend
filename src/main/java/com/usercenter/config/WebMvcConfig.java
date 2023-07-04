package com.usercenter.config;

import com.usercenter.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有请求
                .addPathPatterns("/user/**", "/common/**")
                // 放行登陆和注册请求
                .excludePathPatterns(
                        "/user/register",// 放行注册
                        "/user/login",// 放行登录
                        "/user/recommend",// 放行推荐
                        "/common/download"// 放行文件下载
                );

    }
}
