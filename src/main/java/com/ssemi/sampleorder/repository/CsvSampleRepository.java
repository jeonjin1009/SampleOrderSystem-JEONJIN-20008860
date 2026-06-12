package com.ssemi.sampleorder.repository;

import com.ssemi.sampleorder.model.Sample;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvSampleRepository implements SampleRepository {

    private final String filePath;

    public CsvSampleRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Sample save(Sample sample) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(toCsv(sample));
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sample;
    }

    @Override
    public List<Sample> findAll() {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        List<Sample> samples = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    samples.add(fromCsv(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return samples;
    }

    @Override
    public Optional<Sample> findById(String id) {
        return findAll().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean update(Sample sample) {
        List<Sample> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(sample.getId())) {
                all.set(i, sample);
                found = true;
                break;
            }
        }
        if (!found) return false;
        writeAll(all);
        return true;
    }

    @Override
    public boolean deleteById(String id) {
        List<Sample> all = findAll();
        boolean removed = all.removeIf(s -> s.getId().equals(id));
        if (!removed) return false;
        writeAll(all);
        return true;
    }

    private void writeAll(List<Sample> samples) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Sample s : samples) {
                writer.write(toCsv(s));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String toCsv(Sample s) {
        return s.getId() + "," + s.getName() + "," + s.getAvgProductionTimeMs()
                + "," + s.getYield() + "," + s.getStock();
    }

    private Sample fromCsv(String line) {
        String[] parts = line.split(",", -1);
        return new Sample(
                parts[0],
                parts[1],
                Long.parseLong(parts[2]),
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4])
        );
    }
}
