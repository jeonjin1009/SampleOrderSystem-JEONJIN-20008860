package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringControllerTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        sampleRepository     = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository      = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        monitoringController = new MonitoringController(orderRepository, sampleRepository);
    }

    @Test
    void getOrderCountByStatus_상태별_집계정확() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));
        orderRepository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.RESERVED));
        orderRepository.save(new Order("O003", "S001", "고객C", 3,  OrderStatus.CONFIRMED));
        orderRepository.save(new Order("O004", "S001", "고객D", 7,  OrderStatus.PRODUCING));

        Map<OrderStatus, Long> counts = monitoringController.getOrderCountByStatus();

        assertEquals(2L, counts.get(OrderStatus.RESERVED));
        assertEquals(1L, counts.get(OrderStatus.CONFIRMED));
        assertEquals(1L, counts.get(OrderStatus.PRODUCING));
    }

    @Test
    void getOrderCountByStatus_REJECTED_제외() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.REJECTED));
        orderRepository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.RESERVED));

        Map<OrderStatus, Long> counts = monitoringController.getOrderCountByStatus();

        assertFalse(counts.containsKey(OrderStatus.REJECTED));
        assertEquals(1L, counts.get(OrderStatus.RESERVED));
    }

    @Test
    void getStockStatus_여유_재고충분() {
        // 재고 100, 주문 수량 합계 10 → 재고(100) > 주문합계(10)의 30% → 여유
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 100));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        StockStatus status = monitoringController.getStockStatus("S001");

        assertEquals(StockStatus.SUFFICIENT, status);
    }

    @Test
    void getStockStatus_부족_30퍼센트이하() {
        // 재고 2, 주문 수량 합계 10 → 재고(2) <= 주문합계(10)의 30%(3) → 부족
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 2));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        StockStatus status = monitoringController.getStockStatus("S001");

        assertEquals(StockStatus.SHORT, status);
    }

    @Test
    void getStockStatus_고갈_재고없음() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        StockStatus status = monitoringController.getStockStatus("S001");

        assertEquals(StockStatus.EMPTY, status);
    }
}
