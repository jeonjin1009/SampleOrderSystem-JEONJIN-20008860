package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.SampleController;

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

    // 시료 주문 접수 폼 — sampleId, 고객명, 수량 입력 (없는 sampleId 재입력 루프)
    public void showCreateForm() {
        throw new UnsupportedOperationException("미구현");
    }

    // RESERVED 주문 목록 출력 — 번호 + 주문ID(앞 8자리) + sampleId + 고객명 + 수량
    public void showReservedList() {
        throw new UnsupportedOperationException("미구현");
    }

    // 승인/거절 처리 — RESERVED 목록 출력 후 번호 선택, 승인/거절 선택
    public void showApproveRejectMenu() {
        throw new UnsupportedOperationException("미구현");
    }
}
