package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.view.MainView;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        SampleRepository sampleRepository = new CsvSampleRepository("samples.csv");
        OrderRepository orderRepository = new CsvOrderRepository("orders.csv");
        SampleController sampleController = new SampleController(sampleRepository);
        ProductionLine productionLine = new ProductionLine(orderRepository, sampleRepository);
        OrderController orderController = new OrderController(sampleRepository, orderRepository, productionLine);
        Scanner scanner = new Scanner(System.in);
        MainView mainView = new MainView(sampleController, orderController, scanner);
        mainView.run();
    }
}
