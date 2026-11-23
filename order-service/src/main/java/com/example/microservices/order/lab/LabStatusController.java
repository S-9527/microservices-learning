package com.example.microservices.order.lab;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 实验台状态端点：把 Sentinel 的真实运行状态暴露给前端可视化。
 *
 * <p>Sentinel 的 {@code DegradeRuleManager.getCircuitBreakers()} 是包级私有，
 * 这里用反射读取——学习项目可接受，生产应改用 Micrometer 指标。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LabStatusController {

    private static final String RESOURCE = "createOrder";
    private final LabStats stats;

    @GetMapping("/lab/status")
    public Map<String, Object> status() {
        return Map.of(
                "circuitBreaker", circuitBreakerState(),
                "degradeRules", DegradeRuleManager.getRules().stream().map(this::degradeView).toList(),
                "flowRules", FlowRuleManager.getRules().stream().map(this::flowView).toList(),
                "stats", Map.of(
                        "total", stats.getTotal(),
                        "success", stats.getSuccess(),
                        "fallback", stats.getFallback(),
                        "blocked", stats.getBlocked()
                ),
                "recent", stats.getRecent().stream().map(r -> Map.of(
                        "time", r.time(),
                        "result", r.result(),
                        "detail", r.detail(),
                        "costMs", r.costMs()
                )).toList()
        );
    }

    /** 返回 CLOSED / OPEN / HALF_OPEN，读不到时为 UNKNOWN。 */
    private String circuitBreakerState() {
        try {
            Method m = DegradeRuleManager.class.getDeclaredMethod("getCircuitBreakers", String.class);
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Object> breakers = (List<Object>) m.invoke(null, RESOURCE);
            if (breakers == null || breakers.isEmpty()) {
                return "UNKNOWN";
            }
            Method currentState = breakers.get(0).getClass().getMethod("currentState");
            return String.valueOf(currentState.invoke(breakers.get(0)));
        } catch (Exception ex) {
            log.debug("读取熔断状态失败", ex);
            return "UNKNOWN";
        }
    }

    private Map<String, Object> degradeView(DegradeRule r) {
        return Map.of(
                "resource", r.getResource(),
                "grade", r.getGrade(),
                "count", r.getCount(),
                "timeWindow", r.getTimeWindow(),
                "minRequestAmount", r.getMinRequestAmount()
        );
    }

    private Map<String, Object> flowView(FlowRule r) {
        return Map.of(
                "resource", r.getResource(),
                "grade", r.getGrade(),
                "count", r.getCount()
        );
    }
}
