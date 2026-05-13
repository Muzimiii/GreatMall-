package com.greatmall.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderDTO {

    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private String coverImage;
    private BigDecimal orderAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

