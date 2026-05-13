package com.greatmall.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillOrderMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long userId;
    private Long productId;
    private BigDecimal orderAmount;
    private LocalDateTime requestTime;
}

