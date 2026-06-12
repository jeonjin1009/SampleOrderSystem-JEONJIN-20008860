package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.ProductionController;
import com.ssemi.sampleorder.production.ProductionJob;

import java.util.List;

public class ProductionView {

    private final ProductionController productionController;

    public ProductionView(ProductionController productionController) {
        this.productionController = productionController;
    }

    public void showProductionStatus() {
        System.out.println("[생산 라인 조회]");

        ProductionJob currentJob = productionController.getCurrentJob();
        System.out.println("--- 현재 생산 중 ---");
        if (currentJob == null) {
            System.out.println("현재 생산 중인 작업이 없습니다.");
        } else {
            long remaining = currentJob.getStartTime() + currentJob.getProductionTimeMs() - System.currentTimeMillis();
            String remainingStr = remaining >= 0 ? remaining + "ms" : "완료 처리 중";
            System.out.println("주문ID   : " + currentJob.getOrderId().substring(0, 8));
            System.out.println("시료ID   : " + currentJob.getSampleId());
            System.out.println("생산량   : " + currentJob.getRequiredQty() + "개");
            System.out.println("잔여시간 : " + remainingStr);
        }

        System.out.println();
        System.out.println("--- 대기 중인 작업 ---");
        List<ProductionJob> waitingJobs = productionController.getWaitingJobs();
        if (waitingJobs.isEmpty()) {
            System.out.println("대기 중인 작업이 없습니다.");
        } else {
            System.out.println("번호  주문ID(앞 8자리)  시료ID  생산량");
            System.out.println("------------------------------------------");
            for (int i = 0; i < waitingJobs.size(); i++) {
                ProductionJob job = waitingJobs.get(i);
                System.out.printf("%-6d%-18s%-8s%d개%n",
                        i + 1,
                        job.getOrderId().substring(0, 8),
                        job.getSampleId(),
                        job.getRequiredQty());
            }
        }
    }
}
