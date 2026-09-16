package com.homeordering.common;

import com.homeordering.entity.User;

/**
 * 当前登录用户（由 AuthInterceptor 写入）
 */
public final class AuthContext {

    private static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(User user) {
        CURRENT.set(user);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static User get() {
        return CURRENT.get();
    }

    public static User requireUser() {
        User user = CURRENT.get();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    public static String requireUuid() {
        return requireUser().getUuid();
    }
}
