package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Sample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SampleRepositoryTest {

    @TempDir
    Path tempDir;

    private SampleRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CsvSampleRepository(tempDir.resolve("samples.csv").toString());
    }

    @Test
    void save_저장후_전체조회시_포함된다() {
        Sample sample = new Sample("S001", "테스트시료", 1000L, 0.9, 50);

        repository.save(sample);
        List<Sample> all = repository.findAll();

        assertTrue(all.stream().anyMatch(s -> s.getId().equals("S001")));
    }

    @Test
    void findById_존재하는ID_반환된다() {
        Sample sample = new Sample("S001", "테스트시료", 1000L, 0.9, 50);
        repository.save(sample);

        Sample found = repository.findById("S001").orElse(null);

        assertNotNull(found);
        assertEquals("S001", found.getId());
        assertEquals("테스트시료", found.getName());
    }

    @Test
    void findById_없는ID_빈Optional반환() {
        assertTrue(repository.findById("NONE").isEmpty());
    }

    @Test
    void update_수정후_변경값이_반영된다() {
        repository.save(new Sample("S001", "원래이름", 1000L, 0.9, 50));

        Sample updated = new Sample("S001", "변경이름", 2000L, 0.8, 30);
        repository.update(updated);

        Sample found = repository.findById("S001").orElseThrow();
        assertEquals("변경이름", found.getName());
        assertEquals(30, found.getStock());
    }

    @Test
    void deleteById_삭제후_조회되지않는다() {
        repository.save(new Sample("S001", "테스트시료", 1000L, 0.9, 50));

        repository.deleteById("S001");

        assertTrue(repository.findById("S001").isEmpty());
    }

    @Test
    void deleteById_없는ID_false반환() {
        boolean result = repository.deleteById("NONE");
        assertFalse(result);
    }
}
