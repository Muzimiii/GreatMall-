package com.greatmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillRequest {

    @NotNull
    private Long userId;
}

