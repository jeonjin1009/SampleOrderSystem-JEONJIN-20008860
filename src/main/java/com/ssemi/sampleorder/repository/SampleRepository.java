package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Optional;

public interface SampleRepository {
    Sample save(Sample sample);
    List<Sample> findAll();
    Optional<Sample> findById(String id);
    boolean update(Sample sample);
    boolean deleteById(String id);
}
