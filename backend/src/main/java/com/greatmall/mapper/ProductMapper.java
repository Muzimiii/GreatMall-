package com.greatmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmall.domain.entity.Product;
import com.greatmall.dto.ProductCardDTO;
import com.greatmall.dto.ProductDetailDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("""
            SELECT
                p.id,
                p.name,
                p.subtitle,
                p.price,
                p.seckill_price AS seckillPrice,
                p.stock,
                p.sold_count AS soldCount,
                p.cover_image AS coverImage,
                CASE WHEN a.id IS NULL THEN FALSE ELSE TRUE END AS seckillEnabled,
                a.start_time AS seckillStartTime,
                a.end_time AS seckillEndTime
            FROM product p
            LEFT JOIN seckill_activity a ON a.product_id = p.id AND a.status = 1
            WHERE p.status = 1
            ORDER BY p.id DESC
            LIMIT #{limit}
            """)
    List<ProductCardDTO> listHotProducts(@Param("limit") Integer limit);

    @Select("""
            SELECT
                p.id,
                p.name,
                p.subtitle,
                p.description,
                p.price,
                p.seckill_price AS seckillPrice,
                p.stock,
                p.sold_count AS soldCount,
                p.cover_image AS coverImage,
                CASE WHEN a.id IS NULL THEN FALSE ELSE TRUE END AS seckillEnabled,
                a.start_time AS seckillStartTime,
                a.end_time AS seckillEndTime
            FROM product p
            LEFT JOIN seckill_activity a ON a.product_id = p.id AND a.status = 1
            WHERE p.id = #{productId} AND p.status = 1
            LIMIT 1
            """)
    ProductDetailDTO findProductDetail(@Param("productId") Long productId);
}

