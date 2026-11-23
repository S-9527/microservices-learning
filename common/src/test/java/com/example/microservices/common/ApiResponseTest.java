package com.example.microservices.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void okResponse_shouldBeSuccess() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.code()).isEqualTo(200);
        assertThat(response.data()).isEqualTo("hello");
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void errorResponse_shouldCarryErrorCode() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.SERVICE_UNAVAILABLE, "稍后重试");

        assertThat(response.code()).isEqualTo(503);
        assertThat(response.message()).isEqualTo("稍后重试");
        assertThat(response.data()).isNull();
        assertThat(response.isSuccess()).isFalse();
    }
}
