package com.example.finance.dataset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class HamOrSpamDatasetReader {
    private HamOrSpamDatasetReader() {
    }

    public static List<String> readHamOrSpamDataset(Path datasetRoot) {
        List<String> skippedFiles = new ArrayList<>();
        if (datasetRoot == null || !Files.exists(datasetRoot)) {
            skippedFiles.add("Dataset path not found: " + datasetRoot);
            return skippedFiles;
        }

        try (Stream<Path> paths = Files.walk(datasetRoot)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            if (Files.size(path) == 0) {
                                skippedFiles.add(path.toString());
                            }
                        } catch (IOException ex) {
                            skippedFiles.add(path.toString());
                        }
                    });
        } catch (IOException ex) {
            skippedFiles.add("Failed to walk dataset: " + ex.getMessage());
        }
        return skippedFiles;
    }
}
