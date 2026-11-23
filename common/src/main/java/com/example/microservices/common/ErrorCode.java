package com.example.microservices.common;

/**
 * 全局错误码。
 *
 * <p>约定：2xx 成功；4xx 调用方错误；5xx 服务方错误。
 * 每个错误码同时携带 HTTP 状态码和业务提示，便于各服务保持一致的错误语义。
 */
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    INTERNAL_ERROR(500, "内部服务错误");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
