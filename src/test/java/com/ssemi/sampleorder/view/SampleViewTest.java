package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleViewTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private SampleController sampleController;

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputCapture;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void showList_출력에_ID정렬순서_포함() {
        // S002, S001 순서로 등록
        sampleController = new SampleController(sampleRepository);
        sampleController.addSample("S002", "시료B", 1000L, 0.9);
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        SampleView sampleView = new SampleView(sampleController, scanner);
        sampleView.showList();

        String output = outputCapture.toString();
        int indexS001 = output.indexOf("S001");
        int indexS002 = output.indexOf("S002");
        assertTrue(indexS001 >= 0, "S001이 출력에 포함되어야 한다");
        assertTrue(indexS002 >= 0, "S002가 출력에 포함되어야 한다");
        assertTrue(indexS001 < indexS002, "S001이 S002보다 먼저 출력되어야 한다");
    }

    @Test
    void showSearchResult_검색결과_출력포함() {
        sampleController = new SampleController(sampleRepository);
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        // 검색 키워드 입력 후 "n"으로 루프 종료
        String input = "시료\nn\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        SampleView sampleView = new SampleView(sampleController, scanner);
        sampleView.showSearchResult();

        String output = outputCapture.toString();
        assertTrue(output.contains("시료A"), "검색 결과에 시료 이름이 포함되어야 한다");
    }

    @Test
    void showAddForm_정상입력_등록성공() {
        sampleController = new SampleController(sampleRepository);

        String input = "S001\n시료A\n1000\n0.9\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        SampleView sampleView = new SampleView(sampleController, scanner);
        sampleView.showAddForm();

        assertTrue(sampleController.listSamples().stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void showAddForm_중복ID_재입력후_등록성공() {
        sampleController = new SampleController(sampleRepository);
        // S001을 미리 등록
        sampleController.addSample("S001", "기존시료", 1000L, 0.9);

        // 중복 ID S001 입력 후 새 ID S002로 재입력
        String input = "S001\nS002\n시료B\n2000\n0.8\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        SampleView sampleView = new SampleView(sampleController, scanner);
        sampleView.showAddForm();

        assertTrue(sampleController.listSamples().stream().anyMatch(s -> s.getId().equals("S002")));
    }
}
