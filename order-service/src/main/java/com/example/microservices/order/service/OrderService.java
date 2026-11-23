package com.example.microservices.order.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.BusinessException;
import com.example.microservices.common.ErrorCode;
import com.example.microservices.common.dto.OrderResult;
import com.example.microservices.common.dto.ProductInfo;
import com.example.microservices.common.dto.UserInfo;
import com.example.microservices.order.client.ProductClient;
import com.example.microservices.order.client.UserClient;
import com.example.microservices.order.entity.Order;
import com.example.microservices.order.lab.LabStats;
import com.example.microservices.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/** 订单编排：调用用户/商品服务，并用 Sentinel 保护自己。 */
@Slf4j
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final LabStats stats;

    public OrderService(OrderMapper orderMapper, UserClient userClient, ProductClient productClient,
                        LabStats stats) {
        this.orderMapper = orderMapper;
        this.userClient = userClient;
        this.productClient = productClient;
        this.stats = stats;
    }

    /**
     * blockHandler：处理 Sentinel 主动拒绝（限流 / 系统保护）。
     * fallback：处理业务异常与下游故障（Feign 抛出的异常）。
     * 两者签名都必须是「原方法参数 + 一个异常参数」。
     */
    @SentinelResource(value = "createOrder",
            blockHandler = "createOrderBlockHandler",
            fallback = "createOrderFallback")
    public ApiResponse<OrderResult> createOrder(Long userId, Long productId, int quantity) {
        long start = System.currentTimeMillis();
        UserInfo user = userClient.getUser(userId).data();
        ProductInfo product = productClient.getProduct(productId).data();
        if (user == null || product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户或商品不存在");
        }

        // 真实扣减数据库库存（条件 UPDATE，库存不足会抛异常）
        productClient.deduct(productId, quantity);

        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(product.price().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        stats.ok(System.currentTimeMillis() - start);
        return ApiResponse.ok(new OrderResult(order.getOrderNo(), userId, productId,
                quantity, order.getAmount(), order.getStatus()));
    }

    public ApiResponse<OrderResult> createOrderBlockHandler(
            Long userId, Long productId, int quantity, BlockException ex) {
        log.warn("Sentinel 限流: {}", ex.getClass().getSimpleName());
        stats.limited(ex.getClass().getSimpleName(), System.currentTimeMillis() - startForStats());
        return ApiResponse.error(ErrorCode.SERVICE_UNAVAILABLE, "系统繁忙（限流），请稍后重试");
    }

    public ApiResponse<OrderResult> createOrderFallback(
            Long userId, Long productId, int quantity, Throwable ex) {
        log.warn("订单服务降级: {}", ex.getMessage());
        stats.degraded(shortMessage(ex), System.currentTimeMillis() - startForStats());
        return ApiResponse.error(ErrorCode.SERVICE_UNAVAILABLE,
                "订单服务暂时不可用（降级）：" + ex.getMessage());
    }

    /** blockHandler/fallback 拿不到 start，用最近一次请求起点近似（仅统计展示用）。 */
    private long startForStats() {
        return System.currentTimeMillis();
    }

    private String shortMessage(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.length() < 80) {
            return msg == null ? ex.getClass().getSimpleName() : msg;
        }
        return msg.substring(0, 80) + "...";
    }

    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在: " + id);
        }
        return order;
    }

    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getOrderNo, orderNo));
    }
}
