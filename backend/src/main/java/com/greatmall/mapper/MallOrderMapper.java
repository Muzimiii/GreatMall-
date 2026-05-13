package com.greatmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greatmall.domain.entity.MallOrder;
import com.greatmall.dto.OrderDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {

    @Select("""
            SELECT
                o.order_no AS orderNo,
                o.user_id AS userId,
                o.product_id AS productId,
                p.name AS productName,
                p.cover_image AS coverImage,
                o.order_amount AS orderAmount,
                o.status,
                o.create_time AS createTime,
                o.update_time AS updateTime
            FROM mall_order o
            LEFT JOIN product p ON p.id = o.product_id
            WHERE o.user_id = #{userId}
            ORDER BY o.create_time DESC
            """)
    List<OrderDTO> findOrdersByUserId(@Param("userId") Long userId);
}

