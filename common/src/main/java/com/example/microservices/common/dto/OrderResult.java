package com.example.microservices.common.dto;

import java.math.BigDecimal;

/** 下单结果。amount = price * quantity，由 order-service 组装。 */
public record OrderResult(String orderId, Long userId, Long productId,
                          int quantity, BigDecimal amount, String status) {
}
