package com.ssemi.sampleorder.view;

import com.ssemi.sampleorder.controller.ReleaseController;

import java.util.Scanner;

public class ReleaseView {

    private final ReleaseController releaseController;
    private final Scanner scanner;

    public ReleaseView(ReleaseController releaseController, Scanner scanner) {
        this.releaseController = releaseController;
        this.scanner = scanner;
    }

    public void showReleaseMenu() {
        throw new UnsupportedOperationException("미구현");
    }
}
