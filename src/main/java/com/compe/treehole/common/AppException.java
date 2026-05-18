package com.compe.treehole.common;

public class AppException extends RuntimeException {

    private final int code;

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AppException badRequest(String message) {
        return new AppException(400, message);
    }

    public static AppException unauthorized(String message) {
        return new AppException(401, message);
    }

    public static AppException forbidden(String message) {
        return new AppException(403, message);
    }

    public static AppException notFound(String message) {
        return new AppException(404, message);
    }
}
