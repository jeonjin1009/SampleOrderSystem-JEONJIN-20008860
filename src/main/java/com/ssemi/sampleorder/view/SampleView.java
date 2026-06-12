package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Scanner;

public class SampleView {

    private final SampleController sampleController;
    private final Scanner scanner;

    public SampleView(SampleController sampleController, Scanner scanner) {
        this.sampleController = sampleController;
        this.scanner = scanner;
    }

    public void showAddForm() {
        // ID 먼저 수집 — 중복이면 오류 출력 후 재입력
        String id = "";
        while (true) {
            System.out.print("시료 ID > ");
            String candidate = scanner.nextLine().trim();
            boolean duplicate = sampleController.listSamples().stream()
                    .anyMatch(s -> s.getId().equals(candidate));
            if (!duplicate) { id = candidate; break; }
            System.out.println("오류: 이미 존재하는 시료 ID입니다. 다시 입력해 주세요.");
        }

        System.out.print("이름 > ");
        String name = scanner.nextLine().trim();

        System.out.print("평균생산시간(ms) > ");
        long avgProductionTimeMs = Long.parseLong(scanner.nextLine().trim());

        System.out.print("수율 > ");
        double yield = Double.parseDouble(scanner.nextLine().trim());

        // addSample에서 IllegalArgumentException 발생 시 오류 출력 (동시성 안전망)
        try {
            Sample sample = sampleController.addSample(id, name, avgProductionTimeMs, yield);
            System.out.println("등록 완료: " + sample.getId() + " / " + sample.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    public void showList() {
        List<Sample> samples = sampleController.listSamples();
        if (samples.isEmpty()) {
            System.out.println("등록된 시료가 없습니다.");
            return;
        }
        System.out.println("ID      이름        평균생산시간(ms)  수율   재고");
        System.out.println("--------------------------------------------------");
        for (Sample s : samples) {
            System.out.printf("%-8s%-12s%-18d%-7.2f%d%n",
                    s.getId(), s.getName(), s.getAvgProductionTimeMs(), s.getYield(), s.getStock());
        }
    }

    public void showDeleteForm() {
        System.out.print("삭제할 시료 ID > ");
        String id = scanner.nextLine().trim();
        try {
            sampleController.deleteSample(id);
            System.out.println("삭제 완료: " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    public void showSearchResult() {
        while (true) {
            System.out.print("검색어 > ");
            String keyword = scanner.nextLine().trim();

            List<Sample> results = sampleController.searchByName(keyword);
            if (results.isEmpty()) {
                System.out.println("검색 결과가 없습니다.");
            } else {
                System.out.println("ID      이름        평균생산시간(ms)  수율   재고");
                System.out.println("--------------------------------------------------");
                for (Sample s : results) {
                    System.out.printf("%-8s%-12s%-18d%-7.2f%d%n",
                            s.getId(), s.getName(), s.getAvgProductionTimeMs(), s.getYield(), s.getStock());
                }
            }

            System.out.print("계속 검색하시겠습니까? (y/n) > ");
            String answer = scanner.nextLine().trim();
            if (!"y".equalsIgnoreCase(answer)) {
                break;
            }
        }
    }
}
