package com.example.finance.io;

import com.example.finance.analysis.FinancialDataFrame;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class FinancialCsvReader {
    private FinancialCsvReader() {
    }

    public static FinancialDataFrame readTransactions(Path csvPath) {
        if (csvPath != null && Files.exists(csvPath)) {
            try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
                return parseCsv(reader);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to read CSV from " + csvPath, ex);
            }
        }

        try (InputStream stream = FinancialCsvReader.class.getResourceAsStream(
                "/sample/financial_transactions.csv")) {
            if (stream == null) {
                throw new IllegalStateException("Sample CSV not found in resources.");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                return parseCsv(reader);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read sample CSV.", ex);
        }
    }

    private static FinancialDataFrame parseCsv(BufferedReader reader) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        String header = reader.readLine();
        if (header == null) {
            throw new IllegalArgumentException("CSV file is empty.");
        }

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",", -1);
            if (parts.length < 7) {
                continue;
            }
            Transaction transaction = new Transaction(
                    LocalDate.parse(parts[0].trim()),
                    parts[1].trim(),
                    parts[2].trim(),
                    TransactionType.from(parts[3]),
                    Double.parseDouble(parts[4]),
                    Double.parseDouble(parts[5]),
                    parts[6].trim()
            );
            transactions.add(transaction);
        }

        return new FinancialDataFrame(transactions);
    }
}
