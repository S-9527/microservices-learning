package com.example.microservices.order.client;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.dto.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ApiResponse<ProductInfo> getProduct(@PathVariable("id") Long id);

    @PostMapping("/products/{id}/deduct")
    ApiResponse<ProductInfo> deduct(@PathVariable("id") Long id,
                                    @RequestParam("quantity") int quantity);
}
