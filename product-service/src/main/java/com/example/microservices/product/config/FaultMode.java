package com.example.microservices.product.config;

/** 故障注入模式：OFF 正常 / ERROR 抛异常 / SLOW 延迟响应。 */
public enum FaultMode {
    OFF,
    ERROR,
    SLOW
}
