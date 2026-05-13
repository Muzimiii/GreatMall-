package com.greatmall.constant;

public final class CacheKeys {

    public static final String PRODUCT_BLOOM_KEY = "greatmall:product:bloom";

    private CacheKeys() {
    }

    public static String productDetail(Long productId) {
        return "greatmall:product:detail:" + productId;
    }

    public static String seckillStock(Long productId) {
        return "greatmall:seckill:stock:" + productId;
    }

    public static String seckillUsers(Long productId) {
        return "greatmall:seckill:users:" + productId;
    }
}

