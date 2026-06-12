package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.ReleaseController;
import com.ssemi.sampleorder.model.Order;

import java.util.List;
import java.util.Scanner;

public class ReleaseView {

    private final ReleaseController releaseController;
    private final Scanner scanner;

    public ReleaseView(ReleaseController releaseController, Scanner scanner) {
        this.releaseController = releaseController;
        this.scanner = scanner;
    }

    public void showReleaseMenu() {
        System.out.println("[출고 처리]");

        while (true) {
            List<Order> releasable = releaseController.getReleasableOrders();

            if (releasable.isEmpty()) {
                System.out.println("출고 가능한 주문이 없습니다.");
                return;
            }

            System.out.printf("%-6s%-20s%-8s%-10s%s%n", "번호", "주문ID(앞 8자리)", "시료ID", "고객명", "수량");
            System.out.println("--------------------------------------------------");
            for (int i = 0; i < releasable.size(); i++) {
                Order o = releasable.get(i);
                String shortId = o.getId().length() >= 8 ? o.getId().substring(0, 8) : o.getId();
                System.out.printf("%-6d%-20s%-8s%-10s%d%n",
                        i + 1, shortId, o.getSampleId(), o.getCustomerName(), o.getQuantity());
            }

            System.out.print("선택 (번호, 0: 취소) > ");
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 선택입니다.");
                continue;
            }

            if (choice == 0) {
                return;
            }

            if (choice < 1 || choice > releasable.size()) {
                System.out.println("잘못된 선택입니다.");
                continue;
            }

            Order target = releasable.get(choice - 1);
            releaseController.release(target.getId());
            String shortId = target.getId().length() >= 8 ? target.getId().substring(0, 8) : target.getId();
            System.out.println("출고 완료 (RELEASE) — 주문번호: " + shortId + "...");
        }
    }
}
