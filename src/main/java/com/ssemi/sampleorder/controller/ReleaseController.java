package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.List;

public class ReleaseController {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public ReleaseController(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public Order release(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID: " + orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("CONFIRMED 상태의 주문만 출고 가능합니다. 현재 상태: " + order.getStatus());
        }

        Sample sample = sampleRepository.findById(order.getSampleId()).orElseThrow();
        sample.setStock(sample.getStock() - order.getQuantity());
        sampleRepository.update(sample);

        order.setStatus(OrderStatus.RELEASE);
        orderRepository.update(order);
        return order;
    }

    public List<Order> getReleasableOrders() {
        return orderRepository.findByStatus(OrderStatus.CONFIRMED);
    }
}
