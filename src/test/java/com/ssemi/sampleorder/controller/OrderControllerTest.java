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
    void approveOrder_재고충분_재고불변() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order order = orderController.createOrder("S001", "고객A", 10);

        orderController.approveOrder(order.getId());

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        assertEquals(50, sample.getStock());
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

    @Test
    void approveOrder_CONFIRMED주문있을때_가용재고기준으로PRODUCING전환() {
        // stock=50, 첫 주문 40개 CONFIRMED → 가용 재고 10개
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order orderA = orderController.createOrder("S001", "고객A", 40);
        orderController.approveOrder(orderA.getId());

        // 두 번째 주문 20개 → 가용(10) < 20 → PRODUCING
        Order orderB = orderController.createOrder("S001", "고객B", 20);
        Order approved = orderController.approveOrder(orderB.getId());

        assertEquals(OrderStatus.PRODUCING, approved.getStatus());
    }

    @Test
    void approveOrder_CONFIRMED주문있어도_가용재고충분하면_CONFIRMED전환() {
        // stock=50, 첫 주문 20개 CONFIRMED → 가용 재고 30개
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        Order orderA = orderController.createOrder("S001", "고객A", 20);
        orderController.approveOrder(orderA.getId());

        // 두 번째 주문 30개 → 가용(30) >= 30 → CONFIRMED
        Order orderB = orderController.createOrder("S001", "고객B", 30);
        Order approved = orderController.approveOrder(orderB.getId());

        assertEquals(OrderStatus.CONFIRMED, approved.getStatus());
    }
}
