package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.repository.OrderRepository;

import java.util.List;

public class ReleaseController {

    private final OrderRepository orderRepository;

    public ReleaseController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order release(String orderId) {
        throw new UnsupportedOperationException("미구현");
    }

    public List<Order> getReleasableOrders() {
        throw new UnsupportedOperationException("미구현");
    }
}
