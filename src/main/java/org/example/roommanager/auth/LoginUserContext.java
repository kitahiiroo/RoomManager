package org.example.roommanager.auth;

import org.example.roommanager.dto.LoginUser;
import org.example.roommanager.exception.BusinessException;

/**
 * 保存和获取当前请求对应的登录用户信息（基于 ThreadLocal）
 */
public class LoginUserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }

    /**
     * 获取当前登录用户，如果为空则抛出未登录异常
     */
    public static LoginUser requireLogin() {
        LoginUser user = USER_HOLDER.get();
        if (user == null) {
            throw new BusinessException("UNAUTHORIZED", "未登录");
        }
        return user;
    }

    /**
     * 获取当前登录用户并校验管理员权限
     */
    public static LoginUser requireAdmin() {
        LoginUser user = requireLogin();
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException("FORBIDDEN", "需要管理员权限");
        }
        return user;
    }
}