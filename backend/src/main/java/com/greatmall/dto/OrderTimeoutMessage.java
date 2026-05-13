package com.greatmall.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

@Data
public class OrderTimeoutMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long userId;
    private Long productId;
}

