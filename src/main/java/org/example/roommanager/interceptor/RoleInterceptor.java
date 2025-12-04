package org.example.roommanager.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.roommanager.constant.SecurityConstants;
import org.example.roommanager.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 根据请求头 X-Role 判断是否有权限执行写操作
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String method = request.getMethod();
        // 只对写操作进行拦截：POST / PUT / DELETE
        if ("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method)) {

            String role = request.getHeader(SecurityConstants.HEADER_ROLE);
            if (!SecurityConstants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                throw new BusinessException("NO_PERMISSION",
                        "您没有权限执行该操作（需要管理员角色）");
            }
        }

        // 其他情况放行
        return true;
    }
}