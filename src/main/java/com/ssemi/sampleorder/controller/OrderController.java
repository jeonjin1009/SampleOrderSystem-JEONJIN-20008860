package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

public class OrderController {

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionLine productionLine;

    public OrderController(SampleRepository sampleRepository,
                           OrderRepository orderRepository,
                           ProductionLine productionLine) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionLine = productionLine;
    }

    public Order createOrder(String sampleId, String customerName, int quantity) {
        throw new UnsupportedOperationException("미구현");
    }

    public Order approveOrder(String orderId) {
        throw new UnsupportedOperationException("미구현");
    }

    public Order rejectOrder(String orderId) {
        throw new UnsupportedOperationException("미구현");
    }
}
