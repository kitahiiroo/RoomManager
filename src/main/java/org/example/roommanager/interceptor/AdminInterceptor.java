package org.example.roommanager.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.roommanager.auth.LoginUserContext;
import org.example.roommanager.controller.AuthController;
import org.example.roommanager.dto.LoginUser;
import org.example.roommanager.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * 管理员权限拦截器：拦截需要 ADMIN 权限的接口
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        LoginUser user = LoginUserContext.get();
        if (user == null) {
            // 按理说先经过 LoginInterceptor，不该为 null，这里兜底
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException("FORBIDDEN", "需要管理员权限");
        }
        return true;
    }
}