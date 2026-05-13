package com.greatmall.service;

import com.greatmall.dto.OrderDTO;
import com.greatmall.dto.OrderTimeoutMessage;
import com.greatmall.dto.SeckillOrderMessage;
import java.util.List;

public interface OrderService {

    void createSeckillOrder(SeckillOrderMessage message);

    void cancelTimeoutOrder(OrderTimeoutMessage message);

    List<OrderDTO> listUserOrders(Long userId);

    void payOrder(String orderNo);
}

