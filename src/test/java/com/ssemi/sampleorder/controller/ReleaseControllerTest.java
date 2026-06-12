package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
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
    private ReleaseController releaseController;

    @BeforeEach
    void setUp() {
        orderRepository  = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        releaseController = new ReleaseController(orderRepository);
    }

    @Test
    void release_CONFIRMED주문_RELEASE로전환() {
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
}
