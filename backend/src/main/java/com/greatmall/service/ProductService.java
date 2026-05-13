package com.greatmall.service;

import com.greatmall.dto.ProductCardDTO;
import com.greatmall.dto.ProductDetailDTO;
import java.util.List;

public interface ProductService {

    List<ProductCardDTO> listHotProducts();

    ProductDetailDTO getProductDetail(Long productId);
}

