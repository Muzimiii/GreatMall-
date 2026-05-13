package com.greatmall.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_CREATE_EXCHANGE = "greatmall.order.exchange";
    public static final String ORDER_CREATE_QUEUE = "greatmall.order.create.queue";
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create";

    public static final String ORDER_TTL_EXCHANGE = "greatmall.order.ttl.exchange";
    public static final String ORDER_TTL_QUEUE = "greatmall.order.ttl.queue";
    public static final String ORDER_TTL_ROUTING_KEY = "order.ttl";

    public static final String ORDER_DLX_EXCHANGE = "greatmall.order.dlx.exchange";
    public static final String ORDER_CANCEL_QUEUE = "greatmall.order.cancel.queue";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    @Bean
    public DirectExchange orderCreateExchange() {
        return new DirectExchange(ORDER_CREATE_EXCHANGE);
    }

    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(ORDER_CREATE_QUEUE).build();
    }

    @Bean
    public Binding orderCreateBinding(
            @Qualifier("orderCreateQueue") Queue queue,
            @Qualifier("orderCreateExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderTtlExchange() {
        return new DirectExchange(ORDER_TTL_EXCHANGE);
    }

    @Bean
    public Queue orderTtlQueue(@Value("${greatmall.seckill.order-timeout-minutes:15}") Integer orderTimeoutMinutes) {
        return QueueBuilder.durable(ORDER_TTL_QUEUE)
                .ttl((int) Duration.ofMinutes(orderTimeoutMinutes).toMillis())
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(ORDER_CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderTtlBinding(
            @Qualifier("orderTtlQueue") Queue queue,
            @Qualifier("orderTtlExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_TTL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE);
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public Binding orderCancelBinding(
            @Qualifier("orderCancelQueue") Queue queue,
            @Qualifier("orderDlxExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ORDER_CANCEL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
