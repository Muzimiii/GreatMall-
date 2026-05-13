package com.greatmall.controller;

import com.greatmall.common.ApiResponse;
import com.greatmall.dto.ProductCardDTO;
import com.greatmall.dto.ProductDetailDTO;
import com.greatmall.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductCardDTO>> listProducts() {
        return ApiResponse.success(productService.listHotProducts());
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailDTO> getProductDetail(@PathVariable Long productId) {
        return ApiResponse.success(productService.getProductDetail(productId));
    }
}

