package com.ssemi.sampleorder.model;

public class Sample {

    private String id;
    private String name;
    private long avgProductionTimeMs;
    private double yield;
    private int stock;

    public Sample() {}

    public Sample(String id, String name, long avgProductionTimeMs, double yield, int stock) {
        this.id = id;
        this.name = name;
        this.avgProductionTimeMs = avgProductionTimeMs;
        this.yield = yield;
        this.stock = stock;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getAvgProductionTimeMs() { return avgProductionTimeMs; }
    public void setAvgProductionTimeMs(long avgProductionTimeMs) { this.avgProductionTimeMs = avgProductionTimeMs; }
    public double getYield() { return yield; }
    public void setYield(double yield) { this.yield = yield; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
