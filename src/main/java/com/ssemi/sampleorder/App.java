package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.MonitoringController;
import com.ssemi.sampleorder.controller.OrderController;
import com.ssemi.sampleorder.controller.ProductionController;
import com.ssemi.sampleorder.controller.ReleaseController;
import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Order;
import com.ssemi.sampleorder.model.OrderStatus;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.production.ProductionJob;
import com.ssemi.sampleorder.production.ProductionLine;
import com.ssemi.sampleorder.repository.CsvOrderRepository;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.view.MainView;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        SampleRepository sampleRepository = new CsvSampleRepository("samples.csv");
        OrderRepository orderRepository = new CsvOrderRepository("orders.csv");
        SampleController sampleController = new SampleController(sampleRepository);
        ProductionLine productionLine = new ProductionLine(orderRepository, sampleRepository);
        OrderController orderController = new OrderController(sampleRepository, orderRepository, productionLine);
        ProductionController productionController = new ProductionController(productionLine);
        MonitoringController monitoringController = new MonitoringController(orderRepository, sampleRepository);
        ReleaseController releaseController = new ReleaseController(orderRepository, sampleRepository);

        requeueProducingOrders(orderRepository, sampleRepository, orderController, productionLine);

        Scanner scanner = new Scanner(System.in);
        MainView mainView = new MainView(sampleController, orderController, productionController,
                monitoringController, releaseController, scanner);
        mainView.run();
    }

    private static void requeueProducingOrders(OrderRepository orderRepository,
                                               SampleRepository sampleRepository,
                                               OrderController orderController,
                                               ProductionLine productionLine) {
        List<Order> producingOrders = orderRepository.findByStatus(OrderStatus.PRODUCING);
        if (producingOrders.isEmpty()) return;

        System.out.println("[시스템] 미완료 생산 주문 " + producingOrders.size() + "건을 생산 큐에 재등록합니다.");
        for (Order order : producingOrders) {
            Optional<Sample> sampleOpt = sampleRepository.findById(order.getSampleId());
            if (sampleOpt.isEmpty()) continue;

            Sample sample = sampleOpt.get();
            int requiredQty = orderController.calculateRequiredQty(order.getQuantity(), sample.getYield());
            long productionTimeMs = sample.getAvgProductionTimeMs() * requiredQty;
            productionLine.submit(new ProductionJob(order.getId(), sample.getId(), requiredQty, productionTimeMs));
        }
    }
}
