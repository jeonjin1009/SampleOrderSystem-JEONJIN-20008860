package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Sample;

import java.util.List;
import java.util.Optional;

public class CsvSampleRepository implements SampleRepository {

    public CsvSampleRepository(String filePath) {}

    @Override
    public Sample save(Sample sample) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public List<Sample> findAll() {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public Optional<Sample> findById(String id) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public boolean update(Sample sample) {
        throw new UnsupportedOperationException("미구현");
    }

    @Override
    public boolean deleteById(String id) {
        throw new UnsupportedOperationException("미구현");
    }
}
