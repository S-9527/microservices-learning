package com.example.microservices.product.controller;

import com.example.microservices.product.config.FaultMode;
import com.example.microservices.product.config.FaultModeHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 实验台的故障注入开关：让前端一键触发下游失败/慢响应。 */
@RestController
@RequestMapping("/admin/fault")
@RequiredArgsConstructor
public class FaultController {

    private final FaultModeHolder holder;

    @GetMapping
    public Map<String, String> get() {
        return Map.of("mode", holder.get().name());
    }

    /** mode: OFF / ERROR / SLOW */
    @PostMapping
    public Map<String, String> set(@RequestParam("mode") String mode) {
        FaultMode parsed = FaultMode.valueOf(mode.toUpperCase());
        holder.set(parsed);
        return Map.of("mode", parsed.name());
    }
}
