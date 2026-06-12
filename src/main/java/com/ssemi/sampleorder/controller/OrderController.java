package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.production.ProductionJob;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.List;
import java.util.UUID;

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
        sampleRepository.findById(sampleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 샘플 ID: " + sampleId));

        Order order = new Order(UUID.randomUUID().toString(), sampleId, customerName, quantity, OrderStatus.RESERVED);
        return orderRepository.save(order);
    }

    public Order approveOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID: " + orderId));

        Sample sample = sampleRepository.findById(order.getSampleId()).orElseThrow();

        if (sample.getStock() >= order.getQuantity()) {
            sample.setStock(sample.getStock() - order.getQuantity());
            sampleRepository.update(sample);
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            int shortage = order.getQuantity() - sample.getStock();
            int requiredQty = (int) Math.ceil(shortage / sample.getYield() / 0.9);
            long productionTimeMs = sample.getAvgProductionTimeMs() * requiredQty;

            productionLine.submit(new ProductionJob(order.getId(), sample.getId(), requiredQty, productionTimeMs));
            order.setStatus(OrderStatus.PRODUCING);
        }

        orderRepository.update(order);
        return order;
    }

    public Order rejectOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID: " + orderId));

        order.setStatus(OrderStatus.REJECTED);
        orderRepository.update(order);
        return order;
    }

    public List<Order> listOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
}
