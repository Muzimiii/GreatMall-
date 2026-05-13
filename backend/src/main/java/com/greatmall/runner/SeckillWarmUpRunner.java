package com.greatmall.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.greatmall.constant.CacheKeys;
import com.greatmall.domain.entity.Product;
import com.greatmall.mapper.ProductMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillWarmUpRunner implements ApplicationRunner {

    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments args) {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)
        );
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(CacheKeys.PRODUCT_BLOOM_KEY);
        if (!bloomFilter.isExists()) {
            bloomFilter.tryInit(100_000L, 0.01D);
        }
        for (Product product : products) {
            bloomFilter.add(product.getId());
            String stockKey = CacheKeys.seckillStock(product.getId());
            Boolean exists = stringRedisTemplate.hasKey(stockKey);
            if (Boolean.FALSE.equals(exists)) {
                stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(product.getStock()));
            }
        }
        log.info("Seckill warmup finished, productCount={}", products.size());
    }
}

