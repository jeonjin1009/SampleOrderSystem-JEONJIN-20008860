package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderViewTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private OrderRepository orderRepository;
    private SampleController sampleController;
    private OrderController orderController;
    private ProductionLine productionLine;

    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        productionLine = new ProductionLine(orderRepository, sampleRepository);
        sampleController = new SampleController(sampleRepository);
        orderController = new OrderController(sampleRepository, orderRepository, productionLine);
    }

    @AfterEach
    void tearDown() {
        productionLine.shutdown();
        System.setOut(originalOut);
        System.setIn(System.in);
    }

    private OrderView buildView(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Scanner scanner = new Scanner(System.in);
        return new OrderView(orderController, sampleController, scanner);
    }

    private ByteArrayOutputStream captureOut() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        return baos;
    }

    @Test
    void showCreateForm_정상입력_RESERVED등록() {
        // S001 시료를 먼저 등록
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));

        ByteArrayOutputStream out = captureOut();
        OrderView view = buildView("S001\n홍길동\n10\ny\n");

        view.showCreateForm();

        String output = out.toString();
        assertTrue(
                output.contains("RESERVED") || output.contains("주문 접수 완료"),
                "출력에 'RESERVED' 또는 '주문 접수 완료'가 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showCreateForm_없는sampleId_재입력유도() {
        // S001 시료를 먼저 등록 (두 번째 시도에서 성공)
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));

        ByteArrayOutputStream out = captureOut();
        OrderView view = buildView("NONE\nS001\n홍길동\n10\ny\n");

        view.showCreateForm();

        String output = out.toString();
        assertTrue(
                output.contains("오류") || output.contains("존재하지 않는"),
                "출력에 '오류' 또는 '존재하지 않는'이 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showReservedList_목록출력() {
        // RESERVED 주문을 미리 생성
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));
        orderController.createOrder("S001", "홍길동", 10);

        ByteArrayOutputStream out = captureOut();
        OrderView view = buildView("");

        view.showReservedList();

        String output = out.toString();
        assertTrue(
                output.contains("S001") || output.contains("홍길동"),
                "출력에 sampleId 또는 고객명이 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showReservedList_빈목록_안내메시지() {
        ByteArrayOutputStream out = captureOut();
        OrderView view = buildView("");

        view.showReservedList();

        String output = out.toString();
        assertTrue(
                output.contains("접수된 주문이 없습니다"),
                "출력에 '접수된 주문이 없습니다'가 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showApproveRejectMenu_승인_재고충분_CONFIRMED() {
        // S001 재고 100 설정 후 10개 주문 생성
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 100));
        orderController.createOrder("S001", "홍길동", 10);

        ByteArrayOutputStream out = captureOut();
        // 1번 선택 후 1(승인) 선택
        OrderView view = buildView("1\n1\n");

        view.showApproveRejectMenu();

        String output = out.toString();
        assertTrue(
                output.contains("CONFIRMED"),
                "출력에 'CONFIRMED'가 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showApproveRejectMenu_승인_재고부족_PRODUCING() {
        // S001 재고 0 상태로 10개 주문 생성
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));
        orderController.createOrder("S001", "홍길동", 10);

        ByteArrayOutputStream out = captureOut();
        // 1번 선택 후 1(승인) 선택
        OrderView view = buildView("1\n1\n");

        view.showApproveRejectMenu();

        String output = out.toString();
        assertTrue(
                output.contains("PRODUCING"),
                "출력에 'PRODUCING'이 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showApproveRejectMenu_거절_REJECTED() {
        // 주문 생성
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));
        orderController.createOrder("S001", "홍길동", 10);

        ByteArrayOutputStream out = captureOut();
        // 1번 선택 후 2(거절) 선택
        OrderView view = buildView("1\n2\n");

        view.showApproveRejectMenu();

        String output = out.toString();
        assertTrue(
                output.contains("REJECTED"),
                "출력에 'REJECTED'가 포함되어야 함. 실제 출력: " + output
        );
    }

    @Test
    void showApproveRejectMenu_시료삭제된주문_안내메시지출력_목록에서제외() {
        // 주문 후 시료 삭제
        sampleRepository.save(new Sample("S001", "시료A", 1000L, 0.9, 0));
        orderController.createOrder("S001", "홍길동", 10);
        sampleRepository.deleteById("S001");

        ByteArrayOutputStream out = captureOut();
        OrderView view = buildView("");

        view.showApproveRejectMenu();

        String output = out.toString();
        assertTrue(output.contains("시료가 현재 사라졌습니다"), "시료 삭제 안내 메시지가 포함되어야 함. 실제 출력: " + output);
        assertTrue(output.contains("처리 가능한 주문이 없습니다"), "처리 가능한 주문 없음 메시지가 포함되어야 함. 실제 출력: " + output);
    }
}
