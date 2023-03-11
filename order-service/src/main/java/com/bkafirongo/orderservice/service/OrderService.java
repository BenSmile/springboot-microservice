package com.bkafirongo.orderservice.service;

import com.bkafirongo.orderservice.model.OrderRequest;
import com.bkafirongo.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(Long orderId);
}
