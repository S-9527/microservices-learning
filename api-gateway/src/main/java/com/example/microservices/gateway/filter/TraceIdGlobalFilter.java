package com.example.microservices.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 最简「横切逻辑」示例：生成/透传 traceId 并记录耗时。
 *
 * <p>注意：响应头必须在 chain.filter 之前写入；响应提交后再改 header
 * 会抛 UnsupportedOperationException（WebFlux 的响应头此时已只读）。
 */
@Slf4j
@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    private static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String headerTraceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        final String traceId = (headerTraceId == null || headerTraceId.isBlank())
                ? UUID.randomUUID().toString().replace("-", "").substring(0, 16)
                : headerTraceId;

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(TRACE_HEADER, traceId)
                .build();
        ServerWebExchange mutated = exchange.mutate().request(request).build();
        // 响应头必须在下游响应提交之前设置
        mutated.getResponse().getHeaders().set(TRACE_HEADER, traceId);

        return chain.filter(mutated).doFinally(signal ->
                log.info("traceId={} path={} cost={}ms", traceId,
                        exchange.getRequest().getPath(), System.currentTimeMillis() - start));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
