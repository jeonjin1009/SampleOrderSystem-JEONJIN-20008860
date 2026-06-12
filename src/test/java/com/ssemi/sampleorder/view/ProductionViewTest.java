package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.ProductionController;
import com.ssemi.sampleorder.production.ProductionJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionViewTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void showProductionStatus_생산없음_안내메시지() {
        ProductionController stub = new ProductionController(null) {
            @Override public ProductionJob getCurrentJob() { return null; }
            @Override public List<ProductionJob> getWaitingJobs() { return Collections.emptyList(); }
            @Override public int getWaitingCount() { return 0; }
        };

        ProductionView view = new ProductionView(stub);
        view.showProductionStatus();

        String output = outContent.toString();
        assertTrue(output.contains("현재 생산 중인 작업이 없습니다."), "생산 없음 안내 메시지가 출력되어야 합니다");
    }

    @Test
    void showProductionStatus_생산중_정보출력() {
        ProductionJob fakeJob = new ProductionJob("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "S001", 13, 5000L);
        fakeJob.setStartTime(System.currentTimeMillis());

        ProductionController stub = new ProductionController(null) {
            @Override public ProductionJob getCurrentJob() { return fakeJob; }
            @Override public List<ProductionJob> getWaitingJobs() { return Collections.emptyList(); }
            @Override public int getWaitingCount() { return 0; }
        };

        ProductionView view = new ProductionView(stub);
        view.showProductionStatus();

        String output = outContent.toString();
        assertTrue(output.contains("a1b2c3d4"), "주문ID 앞 8자리가 출력되어야 합니다");
        assertTrue(output.contains("S001"), "시료ID가 출력되어야 합니다");
        assertTrue(output.contains("13"), "생산량이 출력되어야 합니다");
    }

    @Test
    void showProductionStatus_대기없음_안내메시지() {
        ProductionController stub = new ProductionController(null) {
            @Override public ProductionJob getCurrentJob() { return null; }
            @Override public List<ProductionJob> getWaitingJobs() { return Collections.emptyList(); }
            @Override public int getWaitingCount() { return 0; }
        };

        ProductionView view = new ProductionView(stub);
        view.showProductionStatus();

        String output = outContent.toString();
        assertTrue(output.contains("대기 중인 작업이 없습니다."), "대기 없음 안내 메시지가 출력되어야 합니다");
    }

    @Test
    void showProductionStatus_대기있음_목록출력() {
        ProductionJob waitingJob = new ProductionJob("e5f6g7h8-1234-5678-abcd-ef9012345678", "S002", 8, 2000L);

        ProductionController stub = new ProductionController(null) {
            @Override public ProductionJob getCurrentJob() { return null; }
            @Override public List<ProductionJob> getWaitingJobs() { return List.of(waitingJob); }
            @Override public int getWaitingCount() { return 1; }
        };

        ProductionView view = new ProductionView(stub);
        view.showProductionStatus();

        String output = outContent.toString();
        assertTrue(output.contains("e5f6g7h8"), "대기 작업의 주문ID 앞 8자리가 출력되어야 합니다");
    }
}
