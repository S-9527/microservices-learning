package com.example.microservices.common.dto;

/** 跨服务传输的用户信息（脱敏后的最小字段集）。 */
public record UserInfo(Long id, String name, String email) {
}
