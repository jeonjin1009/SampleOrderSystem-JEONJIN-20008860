package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    @TempDir
    Path tempDir;

    private OrderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
    }

    @Test
    void save_저장후_전체조회시_포함된다() {
        Order order = new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED);

        repository.save(order);
        List<Order> all = repository.findAll();

        assertTrue(all.stream().anyMatch(o -> o.getId().equals("O001")));
    }

    @Test
    void findById_존재하는ID_반환된다() {
        repository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        Order found = repository.findById("O001").orElse(null);

        assertNotNull(found);
        assertEquals("O001", found.getId());
        assertEquals(OrderStatus.RESERVED, found.getStatus());
    }

    @Test
    void update_상태변경후_반영된다() {
        repository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        Order updated = new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED);
        repository.update(updated);

        Order found = repository.findById("O001").orElseThrow();
        assertEquals(OrderStatus.CONFIRMED, found.getStatus());
    }

    @Test
    void deleteById_삭제후_조회되지않는다() {
        repository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));

        repository.deleteById("O001");

        assertTrue(repository.findById("O001").isEmpty());
    }

    @Test
    void findByStatus_RESERVED_목록반환() {
        repository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.RESERVED));
        repository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.CONFIRMED));
        repository.save(new Order("O003", "S002", "고객C", 3,  OrderStatus.RESERVED));

        List<Order> reserved = repository.findByStatus(OrderStatus.RESERVED);

        assertEquals(2, reserved.size());
        assertTrue(reserved.stream().allMatch(o -> o.getStatus() == OrderStatus.RESERVED));
    }

    @Test
    void findByStatus_CONFIRMED_목록반환() {
        repository.save(new Order("O001", "S001", "고객A", 10, OrderStatus.CONFIRMED));
        repository.save(new Order("O002", "S001", "고객B", 5,  OrderStatus.RESERVED));

        List<Order> confirmed = repository.findByStatus(OrderStatus.CONFIRMED);

        assertEquals(1, confirmed.size());
        assertEquals("O001", confirmed.get(0).getId());
    }
}
