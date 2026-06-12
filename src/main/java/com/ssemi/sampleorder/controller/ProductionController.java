package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.production.ProductionJob;
import com.ssemi.sampleorder.production.ProductionLine;
import java.util.List;

public class ProductionController {

    private final ProductionLine productionLine;

    public ProductionController(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public ProductionJob getCurrentJob() {
        return productionLine.getCurrentJob();
    }

    public List<ProductionJob> getWaitingJobs() {
        return productionLine.getWaitingJobs();
    }

    public int getWaitingCount() {
        return productionLine.getWaitingJobs().size();
    }
}
