package com.example.microservices.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.microservices.product.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 条件扣减库存：仅当库存足够时才更新。
     * 这一条 SQL 同时完成了「判断 + 扣减」，在数据库层面避免超卖。
     *
     * @return 受影响行数，0 表示库存不足或商品不存在
     */
    @Update("UPDATE products SET stock = stock - #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);
}
