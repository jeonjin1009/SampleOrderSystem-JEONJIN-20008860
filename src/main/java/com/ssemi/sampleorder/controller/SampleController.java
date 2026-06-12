package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SampleController {

    private final SampleRepository sampleRepository;

    public SampleController(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public Sample addSample(String id, String name, long avgProductionTimeMs, double yield) {
        if (sampleRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 시료 ID입니다: " + id);
        }
        Sample sample = new Sample(id, name, avgProductionTimeMs, yield, 0);
        return sampleRepository.save(sample);
    }

    public List<Sample> listSamples() {
        return sampleRepository.findAll().stream()
                .sorted(Comparator.comparing(Sample::getId))
                .collect(Collectors.toList());
    }

    public List<Sample> searchByName(String keyword) {
        return sampleRepository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
