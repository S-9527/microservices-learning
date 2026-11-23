package com.example.microservices.order.controller;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.ErrorCode;
import com.example.microservices.common.dto.OrderResult;
import com.example.microservices.order.entity.Order;
import com.example.microservices.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ApiResponse<OrderResult> createOrder(
            @RequestParam("userId") Long userId,
            @RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        return orderService.createOrder(userId, productId, quantity);
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Order> getOrder(@PathVariable("id") Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return ApiResponse.error(ErrorCode.NOT_FOUND);
        }
        return ApiResponse.ok(order);
    }

    /** 下单成功后用 orderNo 查询，便于验证订单确实落库。 */
    @GetMapping("/orders/no/{orderNo}")
    public ApiResponse<Order> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        return ApiResponse.ok(orderService.getByOrderNo(orderNo));
    }
}
