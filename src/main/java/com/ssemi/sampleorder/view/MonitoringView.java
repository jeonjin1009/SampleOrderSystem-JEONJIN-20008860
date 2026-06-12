package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.MonitoringController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.model.StockStatus;

import java.util.List;
import java.util.Map;

public class MonitoringView {

    private final MonitoringController monitoringController;
    private final SampleController sampleController;

    public MonitoringView(MonitoringController monitoringController, SampleController sampleController) {
        this.monitoringController = monitoringController;
        this.sampleController = sampleController;
    }

    public void showOrderStatus() {
        System.out.println("[주문 현황]");
        Map<OrderStatus, Long> countByStatus = monitoringController.getOrderCountByStatus();

        if (countByStatus.isEmpty()) {
            System.out.println("접수된 주문이 없습니다.");
            return;
        }

        System.out.printf("%-14s%s%n", "상태", "주문 수");
        System.out.println("--------------------");

        OrderStatus[] displayOrder = {
            OrderStatus.RESERVED, OrderStatus.PRODUCING, OrderStatus.CONFIRMED, OrderStatus.RELEASE
        };
        for (OrderStatus status : displayOrder) {
            long count = countByStatus.getOrDefault(status, 0L);
            System.out.printf("%-14s%d%n", status.name(), count);
        }
    }

    public void showStockStatus() {
        System.out.println("[재고 현황]");
        List<Sample> samples = sampleController.listSamples();

        if (samples.isEmpty()) {
            System.out.println("등록된 시료가 없습니다.");
            return;
        }

        System.out.printf("%-9s%-11s%-12s%s%n", "시료ID", "시료명", "현재 재고", "재고 상태");
        System.out.println("------------------------------------------");

        for (Sample sample : samples) {
            StockStatus status = monitoringController.getStockStatus(sample.getId());
            System.out.printf("%-9s%-11s%-12d%s%n",
                    sample.getId(), sample.getName(), sample.getStock(), stockStatusLabel(status));
        }
    }

    public void show() {
        System.out.println("[모니터링]");
        System.out.println();
        showOrderStatus();
        System.out.println();
        showStockStatus();
    }

    private String stockStatusLabel(StockStatus status) {
        switch (status) {
            case SUFFICIENT: return "여유";
            case SHORT: return "부족";
            case EMPTY: return "고갈";
            default: return status.name();
        }
    }
}
