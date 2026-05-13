package com.greatmall.messaging;

import com.greatmall.config.RabbitMQConfig;
import com.greatmall.dto.SeckillOrderMessage;
import com.greatmall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATE_QUEUE)
    public void consume(SeckillOrderMessage message) {
        log.info("Consume seckill order message, orderNo={}", message.getOrderNo());
        orderService.createSeckillOrder(message);
    }
}

