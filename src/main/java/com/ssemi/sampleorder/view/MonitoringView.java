package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.MonitoringController;
import com.ssemi.sampleorder.controller.SampleController;

public class MonitoringView {

    private final MonitoringController monitoringController;
    private final SampleController sampleController;

    public MonitoringView(MonitoringController monitoringController, SampleController sampleController) {
        this.monitoringController = monitoringController;
        this.sampleController = sampleController;
    }

    public void showOrderStatus() {
        throw new UnsupportedOperationException("미구현");
    }

    public void showStockStatus() {
        throw new UnsupportedOperationException("미구현");
    }

    public void show() {
        throw new UnsupportedOperationException("미구현");
    }
}
