package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonitoringController {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public MonitoringController(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public Map<OrderStatus, Long> getOrderCountByStatus() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.REJECTED)
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    public StockStatus getStockStatus(String sampleId) {
        Sample sample = sampleRepository.findById(sampleId).orElseThrow();

        if (sample.getStock() == 0) {
            return StockStatus.EMPTY;
        }

        // 진행 중인 주문(RESERVED + PRODUCING)만 재고 압박 지표로 합산
        int totalOrdered = orderRepository.findAll().stream()
                .filter(o -> o.getSampleId().equals(sampleId))
                .filter(o -> o.getStatus() == OrderStatus.RESERVED || o.getStatus() == OrderStatus.PRODUCING)
                .mapToInt(Order::getQuantity)
                .sum();

        if (sample.getStock() > totalOrdered * 0.3) {
            return StockStatus.SUFFICIENT;
        }
        return StockStatus.SHORT;
    }
}
