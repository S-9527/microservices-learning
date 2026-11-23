package com.example.microservices.product.service;

import com.example.microservices.common.BusinessException;
import com.example.microservices.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void deduct_shouldThrowConflictWhenStockInsufficient() {
        when(productMapper.deductStock(1L, 100)).thenReturn(0);

        assertThatThrownBy(() -> productService.deduct(1L, 100))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    void deduct_shouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> productService.deduct(1L, 0))
                .isInstanceOf(BusinessException.class);

        verify(productMapper, org.mockito.Mockito.never()).deductStock(1L, 0);
    }
}
