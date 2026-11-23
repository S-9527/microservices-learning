package com.example.microservices.product.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/** 保存当前故障模式，供 Controller 在调用业务前注入。 */
@Component
public class FaultModeHolder {

    private final AtomicReference<FaultMode> mode = new AtomicReference<>(FaultMode.OFF);

    public FaultMode get() {
        return mode.get();
    }

    public void set(FaultMode newMode) {
        mode.set(newMode);
    }
}
