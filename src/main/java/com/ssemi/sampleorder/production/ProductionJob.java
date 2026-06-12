package com.ssemi.sampleorder.production;

public class ProductionJob {

    private String orderId;
    private String sampleId;
    private int requiredQty;
    private long productionTimeMs;

    public ProductionJob(String orderId, String sampleId, int requiredQty, long productionTimeMs) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.requiredQty = requiredQty;
        this.productionTimeMs = productionTimeMs;
    }

    public String getOrderId() { return orderId; }
    public String getSampleId() { return sampleId; }
    public int getRequiredQty() { return requiredQty; }
    public long getProductionTimeMs() { return productionTimeMs; }
}
