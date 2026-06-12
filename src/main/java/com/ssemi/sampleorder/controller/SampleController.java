package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.List;

public class SampleController {

    public SampleController(SampleRepository sampleRepository) {
        throw new UnsupportedOperationException("미구현");
    }

    public Sample addSample(String id, String name, long avgProductionTimeMs, double yield) {
        throw new UnsupportedOperationException("미구현");
    }

    public List<Sample> listSamples() {
        throw new UnsupportedOperationException("미구현");
    }

    public List<Sample> searchByName(String keyword) {
        throw new UnsupportedOperationException("미구현");
    }
}
