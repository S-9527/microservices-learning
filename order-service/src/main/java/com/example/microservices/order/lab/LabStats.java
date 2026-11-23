package com.example.microservices.order.lab;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 实验台统计：记录下单请求的实时结果，供前端可视化限流/熔断效果。
 *
 * <p>仅用于学习演示，生产环境应使用 Micrometer 指标 + Prometheus。
 */
@Component
public class LabStats {

    public record Record(long time, String result, String detail, long costMs) {}

    private final AtomicLong total = new AtomicLong();
    private final AtomicLong success = new AtomicLong();
    private final AtomicLong fallback = new AtomicLong();
    private final AtomicLong blocked = new AtomicLong();
    private final Deque<Record> recent = new ArrayDeque<>();

    public void ok(long costMs) {
        total.incrementAndGet();
        success.incrementAndGet();
        add("SUCCESS", "下单成功", costMs);
    }

    public void degraded(String detail, long costMs) {
        total.incrementAndGet();
        fallback.incrementAndGet();
        add("FALLBACK", detail, costMs);
    }

    public void limited(String detail, long costMs) {
        total.incrementAndGet();
        blocked.incrementAndGet();
        add("BLOCKED", detail, costMs);
    }

    private synchronized void add(String result, String detail, long costMs) {
        recent.addFirst(new Record(System.currentTimeMillis(), result, detail, costMs));
        while (recent.size() > 30) {
            recent.removeLast();
        }
    }

    public long getTotal() { return total.get(); }
    public long getSuccess() { return success.get(); }
    public long getFallback() { return fallback.get(); }
    public long getBlocked() { return blocked.get(); }

    public synchronized List<Record> getRecent() {
        return List.copyOf(recent);
    }
}
