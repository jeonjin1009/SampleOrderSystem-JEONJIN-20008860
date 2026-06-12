package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppIntegrationTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private SampleController sampleController;
    private OrderController orderController;
    private ProductionLine productionLine;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine = new ProductionLine(orderRepository, sampleRepository);
        sampleController = new SampleController(sampleRepository);
        orderController = new OrderController(sampleRepository, orderRepository, productionLine);
    }

    @AfterEach
    void tearDown() {
        productionLine.shutdown();
    }

    @Test
    void addSample_후_listSamples_포함확인() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        List<Sample> result = sampleController.listSamples();

        assertTrue(result.stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void addSample_후_searchByName_검색됨() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        List<Sample> result = sampleController.searchByName("시료A");

        assertTrue(result.stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void createOrder_후_approveOrder_재고충분_CONFIRMED() {
        // 재고 100으로 시료 등록
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 100));

        Order order = orderController.createOrder("S001", "홍길동", 10);
        Order approved = orderController.approveOrder(order.getId());

        assertEquals(OrderStatus.CONFIRMED, approved.getStatus());
    }

    @Test
    void createOrder_후_approveOrder_재고부족_PRODUCING() {
        // 재고 0으로 시료 등록
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));

        Order order = orderController.createOrder("S001", "홍길동", 10);
        Order approved = orderController.approveOrder(order.getId());

        assertEquals(OrderStatus.PRODUCING, approved.getStatus());
    }
}
