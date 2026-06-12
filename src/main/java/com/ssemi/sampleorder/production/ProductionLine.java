package com.ssemi.sampleorder.production;

import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.production.ProductionJob;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ProductionLine {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;
    private final LinkedBlockingQueue<ProductionJob> queue;
    private final ExecutorService executor;
    private volatile ProductionJob currentJob;

    public ProductionLine(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.queue = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.submit(this::processLoop);
    }

    public void submit(ProductionJob job) {
        queue.offer(job);
    }

    public ProductionJob getCurrentJob() {
        return currentJob;
    }

    public List<ProductionJob> getWaitingJobs() {
        return new ArrayList<>(queue);
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private void processLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ProductionJob job = queue.take();
                job.setStartTime(System.currentTimeMillis());
                currentJob = job;
                try {
                    Thread.sleep(job.getProductionTimeMs());
                    completeJob(job);
                } finally {
                    currentJob = null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void completeJob(ProductionJob job) {
        Order order = orderRepository.findById(job.getOrderId()).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.update(order);
        }
        Sample sample = sampleRepository.findById(job.getSampleId()).orElse(null);
        if (sample != null) {
            sample.setStock(sample.getStock() + job.getRequiredQty());
            sampleRepository.update(sample);
        }
    }
}
