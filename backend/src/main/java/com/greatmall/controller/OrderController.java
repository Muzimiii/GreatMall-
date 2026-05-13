package com.greatmall.controller;

import com.greatmall.common.ApiResponse;
import com.greatmall.dto.OrderDTO;
import com.greatmall.service.OrderService;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<List<OrderDTO>> listOrders(@RequestParam @NotNull Long userId) {
        return ApiResponse.success(orderService.listUserOrders(userId));
    }

    @PostMapping("/{orderNo}/pay")
    public ApiResponse<Void> pay(@PathVariable String orderNo) {
        orderService.payOrder(orderNo);
        return ApiResponse.success();
    }
}

