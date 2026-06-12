package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionJob;
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

import static org.junit.jupiter.api.Assertions.*;

class ProductionControllerTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private ProductionLine productionLine;
    private ProductionController productionController;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine = new ProductionLine(orderRepository, sampleRepository);
        productionController = new ProductionController(productionLine);
    }

    @AfterEach
    void tearDown() {
        productionLine.shutdown();
    }

    @Test
    void getCurrentJob_생산없음_null반환() {
        assertNull(productionController.getCurrentJob());
    }

    @Test
    void getCurrentJob_생산중_null아님() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));

        productionLine.submit(new ProductionJob("O001", "S001", 5, 100L));
        Thread.sleep(50);

        assertNotNull(productionController.getCurrentJob());
    }

    @Test
    void getWaitingJobs_대기작업_목록반환() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        sampleRepository.save(new Sample("S002", "시료B", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));
        orderRepository.save(new Order("O002", "S002", "고객B", 3, OrderStatus.PRODUCING));

        // 매우 긴 첫 번째 작업으로 큐를 점유하고 두 번째 작업을 대기시킴
        productionLine.submit(new ProductionJob("O001", "S001", 5, 10000L));
        Thread.sleep(50);
        productionLine.submit(new ProductionJob("O002", "S002", 3, 100L));

        assertTrue(productionController.getWaitingJobs().size() >= 1);
    }

    @Test
    void getWaitingCount_대기건수_정확() throws InterruptedException {
        sampleRepository.save(new Sample("S001", "시료A", 100L, 0.9, 0));
        sampleRepository.save(new Sample("S002", "시료B", 100L, 0.9, 0));
        orderRepository.save(new Order("O001", "S001", "고객A", 5, OrderStatus.PRODUCING));
        orderRepository.save(new Order("O002", "S002", "고객B", 3, OrderStatus.PRODUCING));

        productionLine.submit(new ProductionJob("O001", "S001", 5, 10000L));
        Thread.sleep(50);
        productionLine.submit(new ProductionJob("O002", "S002", 3, 100L));

        assertEquals(productionController.getWaitingJobs().size(), productionController.getWaitingCount());
    }
}
