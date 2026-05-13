package com.greatmall.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductCardDTO {

    private Long id;
    private String name;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer soldCount;
    private String coverImage;
    private Boolean seckillEnabled;
    private LocalDateTime seckillStartTime;
    private LocalDateTime seckillEndTime;
}

