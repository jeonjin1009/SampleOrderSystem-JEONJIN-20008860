package com.ssemi.sampleorder.controller;

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

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReleaseControllerTest {

    @TempDir
    Path tempDir;

    private OrderRepository orderRepository;
    private SampleRepository sampleRepository;
    private ReleaseController releaseController;

    @BeforeEach
    void setUp() {
        orderRepository   = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        sampleRepository  = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        releaseController = new ReleaseController(orderRepository, sampleRepository);
    }

    @Test
    void release_CONFIRMED주문_RELEASE로전환() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED));

        Order released = releaseController.release("O001");

        assertEquals(OrderStatus.RELEASE, released.getStatus());
    }

    @Test
    void release_CONFIRMED아닌주문_예외발생() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        assertThrows(Exception.class, () -> releaseController.release("O001"));
    }

    @Test
    void getReleasableOrders_CONFIRMED만반환() {
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED));
        orderRepository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.RESERVED));
        orderRepository.save(new Order("O003", "S001", "고객C", 3,  OrderStatus.CONFIRMED));

        List<Order> releasable = releaseController.getReleasableOrders();

        assertEquals(2, releasable.size());
        assertTrue(releasable.stream().allMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    void release_재고충분케이스_재고차감확인() {
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 50));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED));

        releaseController.release("O001");

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        assertEquals(40, sample.getStock());
    }

    @Test
    void release_재고부족케이스_재고차감확인() {
        // 생산 완료 후 입고된 상태 시뮬레이션: 재고 12
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 12));
        orderRepository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED));

        releaseController.release("O001");

        Sample sample = sampleRepository.findById("S001").orElseThrow();
        assertEquals(2, sample.getStock());
    }
}
