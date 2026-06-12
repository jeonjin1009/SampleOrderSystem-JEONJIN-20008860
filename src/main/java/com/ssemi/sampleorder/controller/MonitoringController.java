package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.Map;

public class MonitoringController {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public MonitoringController(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public Map<OrderStatus, Long> getOrderCountByStatus() {
        throw new UnsupportedOperationException("미구현");
    }

    public StockStatus getStockStatus(String sampleId) {
        throw new UnsupportedOperationException("미구현");
    }
}
