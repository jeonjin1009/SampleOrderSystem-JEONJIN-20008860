package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.ProductionController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Scanner;

public class MainView {

    private final SampleController sampleController;
    private final OrderController orderController;
    private final ProductionController productionController;
    private final SampleView sampleView;
    private final OrderView orderView;
    private final ProductionView productionView;
    private final Scanner scanner;

    public MainView(SampleController sampleController, OrderController orderController,
                    ProductionController productionController, Scanner scanner) {
        this.sampleController = sampleController;
        this.orderController = orderController;
        this.productionController = productionController;
        this.scanner = scanner;
        this.sampleView = new SampleView(sampleController, scanner);
        this.orderView = new OrderView(orderController, sampleController, scanner);
        this.productionView = new ProductionView(productionController);
    }

    public void run() {
        while (true) {
            printSummary();
            System.out.println("=== 메인 메뉴 ===");
            System.out.println("1. 시료 관리");
            System.out.println("2. 시료 주문");
            System.out.println("3. 주문 승인/거절");
            System.out.println("4. 모니터링             (추후 구현)");
            System.out.println("5. 생산 라인 조회");
            System.out.println("6. 출고 처리            (추후 구현)");
            System.out.println("0. 종료");
            System.out.print("선택 > ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    showSampleMenu();
                    break;
                case "2":
                    orderView.showCreateForm();
                    break;
                case "3":
                    orderView.showApproveRejectMenu();
                    break;
                case "4":
                case "6":
                    System.out.println("추후 구현 예정입니다.");
                    break;
                case "5":
                    productionView.showProductionStatus();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void printSummary() {
        List<Sample> samples = sampleController.listSamples();
        int totalSamples = samples.size();
        int totalStock = samples.stream().mapToInt(Sample::getStock).sum();

        long totalOrders = orderController.listOrders().stream()
                .filter(o -> o.getStatus() != OrderStatus.REJECTED)
                .count();

        System.out.println("=== 현황 요약 ===");
        System.out.println("등록 시료 수: " + totalSamples);
        System.out.println("전체 재고 수: " + totalStock);
        System.out.println("전체 주문 수: " + totalOrders);
        System.out.println("생산 대기 건수: " + productionController.getWaitingCount());
    }

    private void showSampleMenu() {
        while (true) {
            System.out.println("=== 시료 관리 ===");
            System.out.println("1. 시료 등록");
            System.out.println("2. 전체 조회");
            System.out.println("3. 이름 검색");
            System.out.println("4. 시료 삭제");
            System.out.println("0. 뒤로");
            System.out.print("선택 > ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    sampleView.showAddForm();
                    break;
                case "2":
                    sampleView.showList();
                    break;
                case "3":
                    sampleView.showSearchResult();
                    break;
                case "4":
                    sampleView.showDeleteForm();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }
}
