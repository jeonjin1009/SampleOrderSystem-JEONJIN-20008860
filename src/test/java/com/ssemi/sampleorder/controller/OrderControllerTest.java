package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private ProductionLine productionLine;
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository  = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine   = new ProductionLine(orderRepository, sampleRepository);
        orderController  = new OrderController(sampleRepository, orderRepository, productionLine);
    }

    @Test
    void createOrder_상태가_RESERVED이다() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));

        Order order = orderController.createOrder("S001", "고객A", 10);

        assertEquals(OrderStatus.RESERVED, order.getStatus());
    }

    @Test
    void createOrder_없는샘플ID_예외발생() {
        assertThrows(IllegalArgumentException.class,
                () -> orderController.createOrder("NONE", "고객A", 10));
    }

    @Test
    void approveOrder_재고충분_CONFIRMED로전환() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order order = orderController.createOrder("S001", "고객A", 10);

        Order approved = orderController.approveOrder(order.getId());

        assertEquals(OrderStatus.CONFIRMED, approved.getStatus());
    }

    @Test
    void approveOrder_재고부족_PRODUCING으로전환() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 5));
        Order order = orderController.createOrder("S001", "고객A", 10);

        Order approved = orderController.approveOrder(order.getId());

        assertEquals(OrderStatus.PRODUCING, approved.getStatus());
    }

    @Test
    void approveOrder_재고충분_재고가차감된다() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order order = orderController.createOrder("S001", "고객A", 10);

        orderController.approveOrder(order.getId());

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        assertEquals(40, sample.getStock());
    }

    @Test
    void rejectOrder_REJECTED로전환() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order order = orderController.createOrder("S001", "고객A", 10);

        Order rejected = orderController.rejectOrder(order.getId());

        assertEquals(OrderStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void approveOrder_없는주문ID_예외발생() {
        assertThrows(IllegalArgumentException.class,
                () -> orderController.approveOrder("NONE"));
    }
}
