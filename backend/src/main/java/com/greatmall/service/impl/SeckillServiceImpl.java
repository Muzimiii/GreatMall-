package com.greatmall.service.impl;

import com.greatmall.common.BusinessException;
import com.greatmall.config.RabbitMQConfig;
import com.greatmall.constant.CacheKeys;
import com.greatmall.dto.ProductDetailDTO;
import com.greatmall.dto.SeckillOrderMessage;
import com.greatmall.service.ProductService;
import com.greatmall.service.SeckillService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class SeckillServiceImpl implements SeckillService {

    private final ProductService productService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final DefaultRedisScript<Long> seckillDeductScript;

    public SeckillServiceImpl(
            ProductService productService,
            StringRedisTemplate stringRedisTemplate,
            RabbitTemplate rabbitTemplate,
            @Qualifier("seckillDeductScript") DefaultRedisScript<Long> seckillDeductScript
    ) {
        this.productService = productService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.seckillDeductScript = seckillDeductScript;
    }

    @Override
    public String createSeckillOrder(Long userId, Long productId) {
        ProductDetailDTO detail = productService.getProductDetail(productId);
        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(detail.getSeckillEnabled())) {
            throw new BusinessException("当前商品未开启秒杀");
        }
        if (detail.getSeckillStartTime() != null && now.isBefore(detail.getSeckillStartTime())) {
            throw new BusinessException("秒杀尚未开始");
        }
        if (detail.getSeckillEndTime() != null && now.isAfter(detail.getSeckillEndTime())) {
            throw new BusinessException("秒杀已结束");
        }

        Long result = stringRedisTemplate.execute(
                seckillDeductScript,
                List.of(CacheKeys.seckillStock(productId), CacheKeys.seckillUsers(productId)),
                String.valueOf(userId)
        );

        if (result == null || result < 0) {
            throw new BusinessException("秒杀系统未预热完成");
        }
        if (result == 0) {
            throw new BusinessException("商品已售罄");
        }
        if (result == 2) {
            throw new BusinessException("同一用户不可重复抢购");
        }

        String orderNo = "GM" + IdWorker.getIdStr();
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setProductId(productId);
        message.setOrderAmount(detail.getSeckillPrice());
        message.setRequestTime(now);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_CREATE_EXCHANGE, RabbitMQConfig.ORDER_CREATE_ROUTING_KEY, message);
        return orderNo;
    }
}
