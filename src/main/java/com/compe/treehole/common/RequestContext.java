package com.compe.treehole.common;

public final class RequestContext {

    private static final ThreadLocal<AuthUser> AUTH_USER = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void set(AuthUser authUser) {
        AUTH_USER.set(authUser);
    }

    public static AuthUser get() {
        AuthUser authUser = AUTH_USER.get();
        if (authUser == null) {
            throw AppException.unauthorized("请先登录或创建匿名会话");
        }
        return authUser;
    }

    public static Long visitorId() {
        AuthUser authUser = get();
        if (!"VISITOR".equals(authUser.role())) {
            throw AppException.forbidden("当前接口需要匿名用户身份");
        }
        return authUser.userId();
    }

    public static Long adminId() {
        AuthUser authUser = get();
        if (!"ADMIN".equals(authUser.role())) {
            throw AppException.forbidden("当前接口需要管理员身份");
        }
        return authUser.userId();
    }

    public static void clear() {
        AUTH_USER.remove();
    }

    public record AuthUser(Long userId, String role, String subject) {
    }
}
