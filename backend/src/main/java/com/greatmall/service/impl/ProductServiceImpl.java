package com.greatmall.service.impl;

import com.greatmall.common.BusinessException;
import com.greatmall.constant.CacheKeys;
import com.greatmall.dto.ProductCardDTO;
import com.greatmall.dto.ProductDetailDTO;
import com.greatmall.mapper.ProductMapper;
import com.greatmall.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedissonClient redissonClient;

    @Value("${greatmall.seckill.hot-product-limit:8}")
    private Integer hotProductLimit;

    @Override
    public List<ProductCardDTO> listHotProducts() {
        return productMapper.listHotProducts(hotProductLimit);
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        validateProductExists(productId);
        String cacheKey = CacheKeys.productDetail(productId);
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedValue)) {
            try {
                return objectMapper.readValue(cachedValue, ProductDetailDTO.class);
            } catch (JsonProcessingException ignored) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        ProductDetailDTO detail = productMapper.findProductDetail(productId);
        if (detail == null) {
            throw new BusinessException("商品不存在");
        }
        try {
            long ttlMinutes = 20 + ThreadLocalRandom.current().nextLong(10);
            stringRedisTemplate.opsForValue().set(
                    cacheKey,
                    objectMapper.writeValueAsString(detail),
                    Duration.ofMinutes(ttlMinutes)
            );
        } catch (JsonProcessingException ignored) {
            // ignore cache serialization failure
        }
        return detail;
    }

    private void validateProductExists(Long productId) {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(CacheKeys.PRODUCT_BLOOM_KEY);
        if (bloomFilter.isExists() && !bloomFilter.contains(productId)) {
            throw new BusinessException("商品不存在");
        }
    }
}

