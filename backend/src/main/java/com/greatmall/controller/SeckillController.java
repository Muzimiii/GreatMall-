package com.greatmall.controller;

import com.greatmall.common.ApiResponse;
import com.greatmall.dto.SeckillRequest;
import com.greatmall.dto.SeckillResultDTO;
import com.greatmall.service.SeckillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @PostMapping("/{productId}")
    public ApiResponse<SeckillResultDTO> seckill(@PathVariable Long productId, @Valid @RequestBody SeckillRequest request) {
        String orderNo = seckillService.createSeckillOrder(request.getUserId(), productId);
        return ApiResponse.success(new SeckillResultDTO(orderNo, "抢购请求已进入异步队列"));
    }
}

