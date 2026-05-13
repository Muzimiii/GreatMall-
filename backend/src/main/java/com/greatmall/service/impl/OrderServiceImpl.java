package com.greatmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.greatmall.common.BusinessException;
import com.greatmall.config.RabbitMQConfig;
import com.greatmall.constant.CacheKeys;
import com.greatmall.constant.OrderStatus;
import com.greatmall.domain.entity.MallOrder;
import com.greatmall.domain.entity.Product;
import com.greatmall.dto.OrderDTO;
import com.greatmall.dto.OrderTimeoutMessage;
import com.greatmall.dto.SeckillOrderMessage;
import com.greatmall.mapper.MallOrderMapper;
import com.greatmall.mapper.ProductMapper;
import com.greatmall.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final MallOrderMapper mallOrderMapper;
    private final ProductMapper productMapper;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillCompensateScript;

    public OrderServiceImpl(
            MallOrderMapper mallOrderMapper,
            ProductMapper productMapper,
            RabbitTemplate rabbitTemplate,
            StringRedisTemplate stringRedisTemplate,
            @Qualifier("seckillCompensateScript") DefaultRedisScript<Long> seckillCompensateScript
    ) {
        this.mallOrderMapper = mallOrderMapper;
        this.productMapper = productMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillCompensateScript = seckillCompensateScript;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(SeckillOrderMessage message) {
        MallOrder existed = mallOrderMapper.selectOne(
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getUserId, message.getUserId())
                        .eq(MallOrder::getProductId, message.getProductId())
                        .in(MallOrder::getStatus, OrderStatus.PENDING_PAY.getCode(), OrderStatus.PAID.getCode())
                        .last("LIMIT 1")
        );
        if (existed != null) {
            return;
        }

        boolean locked = deductStockWithOptimisticLock(message.getProductId());
        if (!locked) {
            compensateRedis(message.getUserId(), message.getProductId());
            throw new BusinessException("库存回写失败，已回滚抢购资格");
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(message.getOrderNo());
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setOrderAmount(message.getOrderAmount());
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setSource("SECKILL");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        try {
            mallOrderMapper.insert(order);
        } catch (DuplicateKeyException exception) {
            return;
        } catch (Exception exception) {
            restoreStockWithOptimisticLock(message.getProductId());
            compensateRedis(message.getUserId(), message.getProductId());
            throw exception;
        }
        stringRedisTemplate.delete(CacheKeys.productDetail(message.getProductId()));

        OrderTimeoutMessage timeoutMessage = new OrderTimeoutMessage();
        timeoutMessage.setOrderNo(message.getOrderNo());
        timeoutMessage.setUserId(message.getUserId());
        timeoutMessage.setProductId(message.getProductId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_TTL_EXCHANGE, RabbitMQConfig.ORDER_TTL_ROUTING_KEY, timeoutMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrder(OrderTimeoutMessage message) {
        MallOrder order = mallOrderMapper.selectOne(
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getOrderNo, message.getOrderNo())
                        .last("LIMIT 1")
        );
        if (order == null || !OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            return;
        }

        int updated = mallOrderMapper.update(
                null,
                new LambdaUpdateWrapper<MallOrder>()
                        .eq(MallOrder::getId, order.getId())
                        .eq(MallOrder::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .set(MallOrder::getStatus, OrderStatus.CANCELED.getCode())
                        .set(MallOrder::getUpdateTime, LocalDateTime.now())
        );
        if (updated == 0) {
            return;
        }

        if (!restoreStockWithOptimisticLock(order.getProductId())) {
            throw new BusinessException("库存释放失败");
        }
        compensateRedis(order.getUserId(), order.getProductId());
    }

    @Override
    public List<OrderDTO> listUserOrders(Long userId) {
        return mallOrderMapper.findOrdersByUserId(userId).stream().peek(order -> {
            order.setStatusText(OrderStatus.getDescByCode(order.getStatus()));
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        MallOrder order = mallOrderMapper.selectOne(
                new LambdaQueryWrapper<MallOrder>()
                        .eq(MallOrder::getOrderNo, orderNo)
                        .last("LIMIT 1")
        );
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (OrderStatus.PAID.getCode().equals(order.getStatus())) {
            return;
        }
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可支付");
        }
        mallOrderMapper.update(
                null,
                new LambdaUpdateWrapper<MallOrder>()
                        .eq(MallOrder::getId, order.getId())
                        .eq(MallOrder::getStatus, OrderStatus.PENDING_PAY.getCode())
                        .set(MallOrder::getStatus, OrderStatus.PAID.getCode())
                        .set(MallOrder::getUpdateTime, LocalDateTime.now())
        );
    }

    private boolean deductStockWithOptimisticLock(Long productId) {
        for (int retry = 0; retry < 3; retry++) {
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStock() == null || product.getStock() <= 0) {
                return false;
            }
            product.setStock(product.getStock() - 1);
            product.setSoldCount((product.getSoldCount() == null ? 0 : product.getSoldCount()) + 1);
            if (productMapper.updateById(product) == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean restoreStockWithOptimisticLock(Long productId) {
        for (int retry = 0; retry < 3; retry++) {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return false;
            }
            product.setStock((product.getStock() == null ? 0 : product.getStock()) + 1);
            product.setSoldCount(Math.max((product.getSoldCount() == null ? 0 : product.getSoldCount()) - 1, 0));
            if (productMapper.updateById(product) == 1) {
                return true;
            }
        }
        return false;
    }

    private void compensateRedis(Long userId, Long productId) {
        Long result = stringRedisTemplate.execute(
                seckillCompensateScript,
                List.of(CacheKeys.seckillStock(productId), CacheKeys.seckillUsers(productId)),
                String.valueOf(userId)
        );
        log.info("Redis compensate finished, productId={}, userId={}, result={}", productId, userId, result);
        stringRedisTemplate.delete(CacheKeys.productDetail(productId));
    }
}
