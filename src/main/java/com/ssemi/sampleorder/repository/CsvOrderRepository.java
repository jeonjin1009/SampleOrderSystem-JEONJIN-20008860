package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CsvOrderRepository implements OrderRepository {

    private final String filePath;

    public CsvOrderRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Order save(Order order) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(toCsv(order));
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return order;
    }

    @Override
    public List<Order> findAll() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        List<Order> orders = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    orders.add(fromCsv(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return orders;
    }

    @Override
    public Optional<Order> findById(String id) {
        return findAll().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized boolean update(Order order) {
        List<Order> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(order.getId())) {
                all.set(i, order);
                found = true;
                break;
            }
        }
        if (!found) return false;
        writeAll(all);
        return true;
    }

    @Override
    public synchronized boolean deleteById(String id) {
        List<Order> all = findAll();
        boolean removed = all.removeIf(o -> o.getId().equals(id));
        if (!removed) return false;
        writeAll(all);
        return true;
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return findAll().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    private void writeAll(List<Order> orders) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Order o : orders) {
                writer.write(toCsv(o));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String toCsv(Order o) {
        return o.getId() + "," + o.getSampleId() + "," + o.getCustomerName()
                + "," + o.getQuantity() + "," + o.getStatus().name();
    }

    private Order fromCsv(String line) {
        String[] parts = line.split(",", -1);
        return new Order(
                parts[0],
                parts[1],
                parts[2],
                Integer.parseInt(parts[3]),
                OrderStatus.valueOf(parts[4])
        );
    }
}
