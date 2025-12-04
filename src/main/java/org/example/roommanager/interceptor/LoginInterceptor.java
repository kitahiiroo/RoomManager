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
 * 登录拦截器：检查是否有登录用户，并放入 LoginUserContext
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        LoginUser loginUser = (LoginUser) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (loginUser == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        // 放到 ThreadLocal，后续业务可以通过 LoginUserContext 获取
        LoginUserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 防止内存泄漏，务必清理
        LoginUserContext.clear();
    }
}