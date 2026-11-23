package com.example.microservices.product.controller;

import com.example.microservices.common.ApiResponse;
import com.example.microservices.common.BusinessException;
import com.example.microservices.common.ErrorCode;
import com.example.microservices.common.dto.ProductInfo;
import com.example.microservices.product.config.FaultMode;
import com.example.microservices.product.config.FaultModeHolder;
import com.example.microservices.product.entity.Product;
import com.example.microservices.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FaultModeHolder faultModeHolder;

    @GetMapping("/products/{id}")
    public ApiResponse<ProductInfo> getProduct(@PathVariable("id") Long id) {
        return ApiResponse.ok(toInfo(productService.getById(id)));
    }

    @PostMapping("/products/{id}/deduct")
    public ApiResponse<ProductInfo> deduct(@PathVariable("id") Long id,
                                           @RequestParam("quantity") int quantity) {
        applyFault();
        return ApiResponse.ok(toInfo(productService.deduct(id, quantity)));
    }

    /** 根据实验台设置的模式注入故障，用于演示熔断降级。 */
    private void applyFault() {
        FaultMode mode = faultModeHolder.get();
        if (mode == FaultMode.ERROR) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "[故障注入] 商品服务模拟异常");
        }
        if (mode == FaultMode.SLOW) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private ProductInfo toInfo(Product product) {
        return new ProductInfo(product.getId(), product.getName(),
                product.getPrice(), product.getStock());
    }
}
