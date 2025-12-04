package org.example.roommanager.config;

import org.example.roommanager.interceptor.AdminInterceptor;
import org.example.roommanager.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册自定义拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AdminInterceptor adminInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor, AdminInterceptor adminInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截：绝大多数 /api/** 都需要登录
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/logout",
                        "/error"
                );

        // 管理员拦截：仅限制增删改类和审批接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/api/classrooms/**",
                        "/api/applications/pending",
                        "/api/applications/*/approve",
                        "/api/applications/*/reject"
                )
                .excludePathPatterns(
                        // 普通用户也能访问的教室查询接口
                        "/api/classrooms",
                        "/api/classrooms/",
                        "/api/classrooms/free",
                        "/api/classrooms/available",
                        "/api/classrooms/*/schedule"
                );
    }
}