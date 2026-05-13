package com.greatmall.messaging;

import com.greatmall.config.RabbitMQConfig;
import com.greatmall.dto.OrderTimeoutMessage;
import com.greatmall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    public void consume(OrderTimeoutMessage message) {
        log.info("Consume timeout cancel message, orderNo={}", message.getOrderNo());
        orderService.cancelTimeoutOrder(message);
    }
}

