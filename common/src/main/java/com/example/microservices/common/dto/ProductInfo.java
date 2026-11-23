package com.example.microservices.common.dto;

import java.math.BigDecimal;

/** 跨服务传输的商品信息。price 使用 BigDecimal，与数据库 DECIMAL 字段对应。 */
public record ProductInfo(Long id, String name, BigDecimal price, int stock) {
}
