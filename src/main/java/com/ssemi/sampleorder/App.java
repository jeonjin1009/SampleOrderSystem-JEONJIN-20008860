package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.view.MainView;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        SampleRepository sampleRepository = new CsvSampleRepository("samples.csv");
        SampleController sampleController = new SampleController(sampleRepository);
        Scanner scanner = new Scanner(System.in);
        MainView mainView = new MainView(sampleController, scanner);
        mainView.run();
    }
}
