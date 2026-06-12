package com.ssemi.sampleorder.production;

import com.ssemi.sampleorder.model.ProductionJob;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

public class ProductionLine {

    private final OrderRepository orderRepository;
    private final SampleRepository sampleRepository;

    public ProductionLine(OrderRepository orderRepository, SampleRepository sampleRepository) {
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
    }

    public void submit(ProductionJob job) {
        throw new UnsupportedOperationException("미구현");
    }

    public ProductionJob getCurrentJob() {
        throw new UnsupportedOperationException("미구현");
    }

    public java.util.List<ProductionJob> getWaitingJobs() {
        throw new UnsupportedOperationException("미구현");
    }

    public void shutdown() {
        throw new UnsupportedOperationException("미구현");
    }
}
