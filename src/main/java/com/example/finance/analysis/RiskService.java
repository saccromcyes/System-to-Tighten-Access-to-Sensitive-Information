package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RiskService {
    private final String currency;

    public RiskService(String currency) {
        this.currency = currency;
    }

    public void printRiskSummary(FinancialDataFrame data) {
        System.out.println("Risk & Anomaly Insights");
        System.out.println("-----------------------");

        List<Transaction> debits = data.transactions().stream()
                .filter(transaction -> transaction.type() == TransactionType.DEBIT)
                .toList();

        DoubleSummaryStatistics stats = debits.stream()
                .collect(Collectors.summarizingDouble(Transaction::amount));

        double mean = stats.getAverage();
        double variance = debits.stream()
                .mapToDouble(transaction -> Math.pow(transaction.amount() - mean, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);

        debits.stream()
                .filter(transaction -> stdDev > 0 && (transaction.amount() - mean) / stdDev > 1.5)
                .sorted(Comparator.comparingDouble(Transaction::amount).reversed())
                .limit(3)
                .forEach(transaction ->
                        System.out.printf("Anomaly: %-18s %s%.2f (%s)%n",
                                transaction.destination(),
                                currencySymbol(),
                                transaction.amount(),
                                transaction.date()));

        Map<String, DoubleSummaryStatistics> categoryStats = data.summarizeByCategory(TransactionType.DEBIT);
        categoryStats.entrySet().stream()
                .sorted((left, right) -> Double.compare(right.getValue().getSum(), left.getValue().getSum()))
                .limit(3)
                .forEach(entry ->
                        System.out.printf("Risk hotspot: %-15s total=%s%.2f%n",
                                entry.getKey(),
                                currencySymbol(),
                                entry.getValue().getSum()));

        double burnRate = stats.getSum() / Math.max(1, data.transactionsByMonth().size());
        System.out.printf("Estimated monthly burn rate: %s%.2f%n", currencySymbol(), burnRate);
        System.out.println();
    }

    private String currencySymbol() {
        return switch (currency.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            default -> currency.toUpperCase() + " ";
        };
    }
}
