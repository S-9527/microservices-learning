package com.example.microservices.product.service;

import com.example.microservices.common.BusinessException;
import com.example.microservices.common.ErrorCode;
import com.example.microservices.product.entity.Product;
import com.example.microservices.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在: " + id);
        }
        return product;
    }

    /** 扣减成功返回最新商品信息；库存不足抛 CONFLICT。 */
    public Product deduct(Long id, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "扣减数量必须大于 0");
        }
        int rows = productMapper.deductStock(id, quantity);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "库存不足或商品不存在: " + id);
        }
        return getById(id);
    }
}
