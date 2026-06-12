package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.ReleaseController;
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
    private ReleaseController releaseController;
    private ProductionLine productionLine;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine = new ProductionLine(orderRepository, sampleRepository);
        sampleController = new SampleController(sampleRepository);
        orderController = new OrderController(sampleRepository, orderRepository, productionLine);
        releaseController = new ReleaseController(orderRepository, sampleRepository);
    }

    @AfterEach
    void tearDown() {
        productionLine.shutdown();
    }

    @Test
    void addSample_후_listSamples_포함확인() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9, 0);

        List<Sample> result = sampleController.listSamples();

        assertTrue(result.stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void addSample_후_searchByName_검색됨() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9, 0);

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

    @Test
    void release_후_상태RELEASE확인() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));

        Order order = orderController.createOrder("S001", "홍길동", 10);
        orderController.approveOrder(order.getId());
        Order released = releaseController.release(order.getId());

        assertEquals(OrderStatus.RELEASE, released.getStatus());
    }

    @Test
    void release_재고부족흐름_재고차감확인() throws InterruptedException {
        // avgProductionTimeMs=10ms로 빠른 생산 완료 유도
        sampleRepository.save(new Sample("S001", "시료A", 10L, 0.9, 0));

        Order order = orderController.createOrder("S001", "홍길동", 5);
        Order approved = orderController.approveOrder(order.getId());
        assertEquals(OrderStatus.PRODUCING, approved.getStatus());

        // 생산 완료 대기: requiredQty 계산 후 생산시간 + 여유
        // shortage=5, requiredQty=ceil(5/0.9/0.9)=7, productionTimeMs=10×7=70ms → 200ms 대기
        Thread.sleep(200);

        // 생산 완료 후 상태가 CONFIRMED로 전환되어 있어야 함
        Order afterProduction = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, afterProduction.getStatus());

        releaseController.release(order.getId());

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        // 생산으로 재고 += requiredQty, 출고로 재고 -= quantity (GREEN에서 구현)
        // RED 단계: release() 시 재고 차감 없으므로 재고 != (requiredQty - 5) → FAIL
        int requiredQty = (int) Math.ceil(5.0 / 0.9 / 0.9);
        assertEquals(requiredQty - 5, sample.getStock());
    }
}
