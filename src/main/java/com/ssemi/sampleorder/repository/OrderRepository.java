package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findAll();
    Optional<Order> findById(String id);
    boolean update(Order order);
    boolean deleteById(String id);
    List<Order> findByStatus(OrderStatus status);
}
