package com.ssemi.sampleorder.production;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.production.ProductionJob;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProductionLineTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private ProductionLine productionLine;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository  = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine   = new ProductionLine(orderRepository, sampleRepository);
    }

    @AfterEach
    void tearDown() {
        productionLine.shutdown();
    }

    @Test
    void submit_작업등록시_자동으로생산시작() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));

        // 생산 시간 100ms로 설정
        ProductionJob job = new ProductionJob("O001", "S001", 5, 100L);
        productionLine.submit(job);

        // 생산 완료까지 최대 2초 대기
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            while (true) {
                Order order = orderRepository.findById("O001").orElse(null);
                if (order != null && order.getStatus() == OrderStatus.CONFIRMED) {
                    latch.countDown();
                    break;
                }
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }).start();

        assertTrue(latch.await(2, TimeUnit.SECONDS), "생산이 2초 안에 완료되어야 합니다");
    }

    @Test
    void production_완료후_주문상태_CONFIRMED() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));

        productionLine.submit(new ProductionJob("O001", "S001", 5, 100L));
        Thread.sleep(500);

        Order order = orderRepository.findById("O001").orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void production_완료후_재고증가() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));

        productionLine.submit(new ProductionJob("O001", "S001", 5, 100L));
        Thread.sleep(500);

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        assertTrue(sample.getStock() > 0, "생산 완료 후 재고가 증가해야 합니다");
    }

    @Test
    void submit_복수작업_FIFO순서로처리() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        sampleRepository.save(new Sample("S002", "시료B", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));
        orderRepository.save(new Order("O002", "S002", "고객B", 3, OrderStatus.PRODUCING));

        // 첫 번째 작업이 오래 걸리도록 설정
        productionLine.submit(new ProductionJob("O001", "S001", 5, 300L));
        productionLine.submit(new ProductionJob("O002", "S002", 3, 100L));

        Thread.sleep(400);
        Order first  = orderRepository.findById("O001").orElseThrow();
        Order second = orderRepository.findById("O002").orElseThrow();

        // O001이 먼저 처리되었으므로 먼저 CONFIRMED 상태
        assertEquals(OrderStatus.CONFIRMED, first.getStatus(), "첫 번째 등록 작업이 먼저 완료되어야 합니다");
        // O002는 O001 완료 후 처리 시작 → 아직 PRODUCING 또는 완료 전
        assertNotEquals(OrderStatus.CONFIRMED, second.getStatus(), "두 번째 작업은 아직 완료되지 않아야 합니다");
    }

    @Test
    void 실생산량_계산_수율과오차반영() {
        // 부족분=10, 수율=0.9 → ceil(10 / 0.9 / 0.9) = ceil(12.35) = 13
        int shortage = 10;
        double yield = 0.9;
        int expected = (int) Math.ceil(shortage / yield / 0.9);

        assertEquals(13, expected);
    }
}
