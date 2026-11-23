package com.example.microservices.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 以代码方式注册 Sentinel 规则，保证不开控制台也有基础保护。
 * 启动控制台后，可在 Dashboard 里动态调整这些规则（内存中生效，重启失效）。
 */
@Configuration
public class SentinelRuleConfig {

    public static final String CREATE_ORDER = "createOrder";

    @PostConstruct
    public void initRules() {
        // 限流：单实例 QPS 超过 20 直接拒绝（触发 blockHandler）
        FlowRule flowRule = new FlowRule(CREATE_ORDER)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(20);
        FlowRuleManager.loadRules(List.of(flowRule));

        // 熔断：异常比例超过 50%（至少 3 个请求）后熔断 10 秒（触发 fallback）
        DegradeRule degradeRule = new DegradeRule(CREATE_ORDER)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)
                .setTimeWindow(10)
                .setMinRequestAmount(3)
                .setStatIntervalMs(10_000);
        DegradeRuleManager.loadRules(List.of(degradeRule));
    }
}
