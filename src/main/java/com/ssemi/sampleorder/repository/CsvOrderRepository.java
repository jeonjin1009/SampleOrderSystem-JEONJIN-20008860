package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public class CsvOrderRepository implements OrderRepository {

    public CsvOrderRepository(String filePath) {}

    @Override
    public Order save(Order order) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public List<Order> findAll() {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public Optional<Order> findById(String id) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public boolean update(Order order) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public boolean deleteById(String id) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        throw new UnsupportedOperationException("미구현");
    }
}
