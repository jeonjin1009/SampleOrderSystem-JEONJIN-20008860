package com.ssemi.sampleorder;

import com.ssemi.sampleorder.controller.SampleController;
import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppIntegrationTest {

    @TempDir
    Path tempDir;

    private SampleController sampleController;

    @BeforeEach
    void setUp() {
        SampleRepository sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        sampleController = new SampleController(sampleRepository);
    }

    @Test
    void addSample_후_listSamples_포함확인() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        List<Sample> result = sampleController.listSamples();

        assertTrue(result.stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void addSample_후_searchByName_검색됨() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        List<Sample> result = sampleController.searchByName("시료A");

        assertTrue(result.stream().anyMatch(s -> s.getId().equals("S001")));
    }
}
