package com.example.microservices.order.client;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** name 是 Nacos 中的服务名，由 LoadBalancer 解析成真实实例地址。 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    ApiResponse<UserInfo> getUser(@PathVariable("id") Long id);
}
