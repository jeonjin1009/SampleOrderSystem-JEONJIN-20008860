package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.MonitoringController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitoringViewTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private MonitoringController monitoringController;
    private SampleController sampleController;
    private MonitoringView monitoringView;

    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        monitoringController = new MonitoringController(orderRepository, sampleRepository);
        sampleController = new SampleController(sampleRepository);
        monitoringView = new MonitoringView(monitoringController, sampleController);

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void showOrderStatus_주문있음_상태별집계출력() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));
        orderRepository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.PRODUCING));
        orderRepository.save(new Order("O003", "S001", "고객C", 3,  OrderStatus.CONFIRMED));
        orderRepository.save(new Order("O004", "S001", "고객D", 7,  OrderStatus.RELEASE));

        monitoringView.showOrderStatus();

        String output = outContent.toString();
        assertTrue(output.contains("RESERVED"));
        assertTrue(output.contains("PRODUCING"));
        assertTrue(output.contains("CONFIRMED"));
        assertTrue(output.contains("RELEASE"));
        assertTrue(output.contains("1"));
    }

    @Test
    void showOrderStatus_REJECTED_제외() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.REJECTED));

        monitoringView.showOrderStatus();

        String output = outContent.toString();
        assertFalse(output.contains("REJECTED"));
    }

    @Test
    void showStockStatus_SUFFICIENT_표기() {
        // 재고=100, RESERVED 주문 quantity=10 → 100 > 10×0.3=3 → SUFFICIENT
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 100));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        monitoringView.showStockStatus();

        String output = outContent.toString();
        assertTrue(output.contains("여유"));
    }

    @Test
    void showStockStatus_SHORT_표기() {
        // 재고=1, RESERVED 주문 quantity=100 → 1 ≤ 100×0.3=30 → SHORT
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 1));
        orderRepository.save(new Order("O001", "S001", "고객A", 100, OrderStatus.RESERVED));

        monitoringView.showStockStatus();

        String output = outContent.toString();
        assertTrue(output.contains("부족"));
    }

    @Test
    void showStockStatus_EMPTY_표기() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));

        monitoringView.showStockStatus();

        String output = outContent.toString();
        assertTrue(output.contains("고갈"));
    }
}
