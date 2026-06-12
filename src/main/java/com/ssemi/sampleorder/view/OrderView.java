package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Scanner;

public class OrderView {

    private final OrderController orderController;
    private final SampleController sampleController;
    private final Scanner scanner;

    public OrderView(OrderController orderController,
                     SampleController sampleController,
                     Scanner scanner) {
        this.orderController = orderController;
        this.sampleController = sampleController;
        this.scanner = scanner;
    }

    public void showCreateForm() {
        System.out.println("[시료 주문]");
        String sampleId;
        while (true) {
            System.out.print("시료 ID  > ");
            sampleId = scanner.nextLine().trim();
            if (sampleController.findSampleById(sampleId).isPresent()) {
                break;
            }
            System.out.println("오류: 존재하지 않는 시료 ID입니다. 다시 입력해 주세요.");
        }

        System.out.print("고객명   > ");
        String customerName = scanner.nextLine().trim();

        int quantity;
        while (true) {
            System.out.print("주문 수량 > ");
            String qtyInput = scanner.nextLine().trim();
            try {
                quantity = Integer.parseInt(qtyInput);
                if (quantity <= 0) {
                    System.out.println("오류: 1 이상의 수량을 입력해 주세요.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("오류: 숫자를 입력해 주세요.");
            }
        }

        System.out.println();
        System.out.println("시료 ID: " + sampleId + " / 고객명: " + customerName + " / 주문수량: " + quantity);
        System.out.print("이 입력하신 내용과 동일합니까? (y/n) > ");
        String confirm = scanner.nextLine().trim();
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("주문이 취소되었습니다.");
            return;
        }

        Order order = orderController.createOrder(sampleId, customerName, quantity);
        System.out.println("주문 접수 완료 (주문번호: " + order.getId().substring(0, 8) + "...) — RESERVED");
    }

    public void showReservedList() {
        System.out.println("[주문 승인/거절]");
        List<Order> reserved = orderController.findOrdersByStatus(OrderStatus.RESERVED);
        if (reserved.isEmpty()) {
            System.out.println("접수된 주문이 없습니다.");
            return;
        }
        System.out.printf("%-4s  %-16s  %-6s  %-8s  %s%n", "번호", "주문ID(앞 8자리)", "시료ID", "고객명", "수량");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < reserved.size(); i++) {
            Order o = reserved.get(i);
            System.out.printf("%-4d  %-16s  %-6s  %-8s  %d%n",
                    i + 1,
                    o.getId().substring(0, 8),
                    o.getSampleId(),
                    o.getCustomerName(),
                    o.getQuantity());
        }
    }

    public void showApproveRejectMenu() {
        List<Order> reserved = orderController.findOrdersByStatus(OrderStatus.RESERVED);
        System.out.println("[주문 승인/거절]");
        if (reserved.isEmpty()) {
            System.out.println("접수된 주문이 없습니다.");
            return;
        }
        System.out.printf("%-4s  %-16s  %-6s  %-8s  %s%n", "번호", "주문ID(앞 8자리)", "시료ID", "고객명", "수량");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < reserved.size(); i++) {
            Order o = reserved.get(i);
            System.out.printf("%-4d  %-16s  %-6s  %-8s  %d%n",
                    i + 1,
                    o.getId().substring(0, 8),
                    o.getSampleId(),
                    o.getCustomerName(),
                    o.getQuantity());
        }

        System.out.print("선택 (번호, 0: 취소) > ");
        String sel = scanner.nextLine().trim();
        int idx;
        try {
            idx = Integer.parseInt(sel);
        } catch (NumberFormatException e) {
            System.out.println("잘못된 선택입니다.");
            return;
        }

        if (idx == 0) return;
        if (idx < 1 || idx > reserved.size()) {
            System.out.println("잘못된 선택입니다.");
            return;
        }

        Order selected = reserved.get(idx - 1);
        String orderId = selected.getId();

        Sample sample = sampleController.findSampleById(selected.getSampleId()).orElseThrow();
        int stock = sample.getStock();
        int quantity = selected.getQuantity();
        if (stock >= quantity) {
            System.out.println("[재고 현황] 현재 재고 " + stock + "개 — 주문 수량(" + quantity + "개) 충족. 생산이 필요하지 않습니다.");
        } else {
            int shortage = quantity - stock;
            int required = (int) Math.ceil(shortage / sample.getYield() / 0.9);
            System.out.println("[재고 현황] 현재 재고 " + stock + "개 — 부족(" + shortage + "개 부족). 생산이 필요합니다. (생산 필요량: " + required + "개)");
        }

        System.out.println("처리 선택 > 1. 승인 / 2. 거절");
        System.out.print("선택 > ");
        String action = scanner.nextLine().trim();

        if ("1".equals(action)) {
            orderController.approveOrder(orderId);

            // approveOrder 후 주문 상태로 분기
            Order updated = orderController.listOrders().stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .orElseThrow();

            if (updated.getStatus() == OrderStatus.CONFIRMED) {
                System.out.println("승인 완료 (CONFIRMED) — 재고 차감: " + quantity + "개");
            } else {
                int shortage = quantity - stock;
                int produced = (int) Math.ceil(shortage / sample.getYield() / 0.9);
                long estimatedMs = sample.getAvgProductionTimeMs() * produced;
                System.out.println("승인 완료 (PRODUCING) — 생산 등록: " + produced + "개 / 예상 시간: " + estimatedMs + "ms");
            }
        } else if ("2".equals(action)) {
            orderController.rejectOrder(orderId);
            System.out.println("거절 완료 (REJECTED)");
        }
    }
}
