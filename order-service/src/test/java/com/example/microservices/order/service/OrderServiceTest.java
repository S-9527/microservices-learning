package com.example.microservices.order.service;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.dto.OrderResult;
import com.example.microservices.common.dto.ProductInfo;
import com.example.microservices.common.dto.UserInfo;
import com.example.microservices.order.client.ProductClient;
import com.example.microservices.order.client.UserClient;
import com.example.microservices.order.entity.Order;
import com.example.microservices.order.lab.LabStats;
import com.example.microservices.order.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private UserClient userClient;
    @Mock private ProductClient productClient;
    @Mock private LabStats stats;

    @InjectMocks private OrderService orderService;

    @Test
    void createOrder_shouldCallDependenciesAndPersistOrder() {
        when(userClient.getUser(1L))
                .thenReturn(ApiResponse.ok(new UserInfo(1L, "alice", "alice@example.com")));
        when(productClient.getProduct(2L))
                .thenReturn(ApiResponse.ok(new ProductInfo(2L, "机械键盘", new BigDecimal("399.00"), 10)));
        when(productClient.deduct(2L, 2))
                .thenReturn(ApiResponse.ok(new ProductInfo(2L, "机械键盘", new BigDecimal("399.00"), 8)));

        ApiResponse<OrderResult> result = orderService.createOrder(1L, 2L, 2);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.data().amount()).isEqualByComparingTo(new BigDecimal("798.00"));
        verify(productClient).deduct(2L, 2);
        verify(orderMapper).insert(any(Order.class));
    }
}
