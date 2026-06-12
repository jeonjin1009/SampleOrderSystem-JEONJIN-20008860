package com.ssemi.sampleorder.controller;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.CsvSampleRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleControllerTest {

    @TempDir
    Path tempDir;

    private SampleRepository sampleRepository;
    private SampleController sampleController;

    @BeforeEach
    void setUp() {
        sampleRepository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
        sampleController = new SampleController(sampleRepository);
    }

    @Test
    void addSample_정상등록_반환값확인() {
        Sample sample = sampleController.addSample("S001", "시료A", 1000L, 0.9);

        assertEquals("S001", sample.getId());
        assertEquals("시료A", sample.getName());
        assertEquals(0.9, sample.getYield());
        assertEquals(0, sample.getStock());
    }

    @Test
    void addSample_중복ID_예외발생() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        assertThrows(IllegalArgumentException.class,
                () -> sampleController.addSample("S001", "시료B", 2000L, 0.8));
    }

    @Test
    void addSample_중복이름_예외발생() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        assertThrows(IllegalArgumentException.class,
                () -> sampleController.addSample("S002", "시료A", 2000L, 0.8));
    }

    @Test
    void listSamples_ID오름차순정렬() {
        sampleController.addSample("S003", "시료C", 1000L, 0.9);
        sampleController.addSample("S001", "시료A", 1000L, 0.9);
        sampleController.addSample("S002", "시료B", 1000L, 0.9);

        List<Sample> result = sampleController.listSamples();

        assertEquals(3, result.size());
        assertEquals("S001", result.get(0).getId());
        assertEquals("S002", result.get(1).getId());
        assertEquals("S003", result.get(2).getId());
    }

    @Test
    void listSamples_빈목록_빈리스트반환() {
        List<Sample> result = sampleController.listSamples();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByName_키워드포함_반환() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);
        sampleController.addSample("S002", "시료B", 1000L, 0.9);
        sampleController.addSample("S003", "다른이름", 1000L, 0.9);

        List<Sample> result = sampleController.searchByName("시료");

        assertEquals(2, result.size());
    }

    @Test
    void searchByName_없는키워드_빈목록() {
        sampleController.addSample("S001", "시료A", 1000L, 0.9);

        List<Sample> result = sampleController.searchByName("없는키워드");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchByName_대소문자무시() {
        sampleController.addSample("S001", "SampleA", 1000L, 0.9);

        List<Sample> result = sampleController.searchByName("samplea");

        assertEquals(1, result.size());
    }
}
