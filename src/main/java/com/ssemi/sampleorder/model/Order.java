package com.ssemi.sampleorder.model;

public class Order {

    private String id;
    private String sampleId;
    private String customerName;
    private int quantity;
    private OrderStatus status;

    public Order() {}

    public Order(String id, String sampleId, String customerName, int quantity, OrderStatus status) {
        this.id = id;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
