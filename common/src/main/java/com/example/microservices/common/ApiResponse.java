package com.example.microservices.common;

/**
 * 所有 HTTP 接口的统一响应包装。
 *
 * @param code    业务状态码（与 ErrorCode 的 httpStatus 对齐）
 * @param message 提示信息
 * @param data    业务数据，失败时为 null
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getHttpStatus(),
                ErrorCode.SUCCESS.getDefaultMessage(), data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getHttpStatus(), message, data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getHttpStatus(), errorCode.getDefaultMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getHttpStatus(), message, null);
    }

    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }
}
