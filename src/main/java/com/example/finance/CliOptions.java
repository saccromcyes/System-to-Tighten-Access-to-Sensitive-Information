package com.example.finance;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record CliOptions(Path csvPath, double threshold, int previewLimit, String hamSpamPath,
                         Path exportDir, String currency) {
    private static final double DEFAULT_THRESHOLD = 500.0;
    private static final int DEFAULT_LIMIT = 5;
    private static final String DEFAULT_HAM_SPAM_PATH = "path/to/dataset";
    private static final String DEFAULT_CURRENCY = "USD";

    public static CliOptions parse(String[] args) {
        Map<String, String> parsed = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                parsed.put(args[i], args[i + 1]);
                i++;
            }
        }

        Path csvPath = parsed.containsKey("--csv")
                ? Path.of(parsed.get("--csv"))
                : null;
        double threshold = parsed.containsKey("--threshold")
                ? Double.parseDouble(parsed.get("--threshold"))
                : DEFAULT_THRESHOLD;
        int limit = parsed.containsKey("--limit")
                ? Integer.parseInt(parsed.get("--limit"))
                : DEFAULT_LIMIT;
        String hamSpamPath = parsed.getOrDefault("--ham-spam-path", DEFAULT_HAM_SPAM_PATH);
        Path exportDir = parsed.containsKey("--export-dir")
                ? Path.of(parsed.get("--export-dir"))
                : Path.of("out");
        String currency = parsed.getOrDefault("--currency", DEFAULT_CURRENCY);

        return new CliOptions(csvPath, threshold, limit, hamSpamPath, exportDir, currency);
    }
}
