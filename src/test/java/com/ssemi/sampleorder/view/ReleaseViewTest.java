package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.ReleaseController;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseViewTest {

    @TempDir
    Path tempDir;

    private OrderRepository orderRepository;
    private SampleRepository sampleRepository;
    private ReleaseController releaseController;

    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        orderRepository = new CsvOrderRepository(tempDir.resolve("orders.csv").toString());
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        // RED 단계: 현재 시그니처 유지 (GREEN에서 sampleRepository 추가 예정)
        releaseController = new ReleaseController(orderRepository);
    }

    @Test
    void showReleaseMenu_정상출고_RELEASE전환() {
        orderRepository.save(new Order("O001", "S001", "홍길동", 10, OrderStatus.CONFIRMED));

        ByteArrayInputStream in = new ByteArrayInputStream("1\n".getBytes());
        Scanner scanner = new Scanner(in);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReleaseView releaseView = new ReleaseView(releaseController, scanner);
        releaseView.showReleaseMenu();

        String output = outContent.toString();
        assertTrue(output.contains("RELEASE"));
    }

    @Test
    void showReleaseMenu_빈목록_안내메시지() {
        ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
        Scanner scanner = new Scanner(in);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReleaseView releaseView = new ReleaseView(releaseController, scanner);
        releaseView.showReleaseMenu();

        String output = outContent.toString();
        assertTrue(output.contains("출고 가능한 주문이 없습니다"));
    }

    @Test
    void showReleaseMenu_취소_0입력() {
        orderRepository.save(new Order("O001", "S001", "홍길동", 10, OrderStatus.CONFIRMED));

        ByteArrayInputStream in = new ByteArrayInputStream("0\n".getBytes());
        Scanner scanner = new Scanner(in);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReleaseView releaseView = new ReleaseView(releaseController, scanner);
        releaseView.showReleaseMenu();

        String output = outContent.toString();
        assertFalse(output.contains("RELEASE"));
    }
}
