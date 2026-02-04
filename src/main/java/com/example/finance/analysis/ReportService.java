package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.time.YearMonth;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    public void analyzeExpenses(FinancialDataFrame data) {
        System.out.println("Expense Overview");
        System.out.println("----------------");
        DoubleSummaryStatistics stats = data.summarize(TransactionType.DEBIT);
        System.out.printf("Total expenses: %.2f%n", stats.getSum());
        System.out.printf("Average expense: %.2f%n", stats.getAverage());
        System.out.printf("Largest expense: %.2f%n", stats.getMax());
        System.out.println();
    }

    public void identifyHighValueTransactions(FinancialDataFrame data, double threshold) {
        System.out.println("High-Value Transactions");
        System.out.println("-----------------------");
        data.filterHighValueTransactions(threshold)
                .forEach(transaction -> System.out.println(" - " + transaction));
        System.out.println();
    }

    public void categorizeTransactions(FinancialDataFrame data) {
        System.out.println("Category Breakdown");
        System.out.println("------------------");
        Map<String, DoubleSummaryStatistics> summary = data.summarizeByCategory(TransactionType.DEBIT);
        summary.forEach((category, stats) ->
                System.out.printf("%-15s total=%.2f average=%.2f%n",
                        category,
                        stats.getSum(),
                        stats.getAverage()));
        System.out.println();
    }

    public void generateMonthlyReport(FinancialDataFrame data) {
        System.out.println("Monthly Report");
        System.out.println("--------------");
        Map<YearMonth, Map<TransactionType, DoubleSummaryStatistics>> monthly = data.transactions().stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date()),
                        Collectors.groupingBy(Transaction::type,
                                Collectors.summarizingDouble(Transaction::amount))));

        monthly.forEach((month, statsByType) -> {
            double income = statsByType.getOrDefault(TransactionType.CREDIT, new DoubleSummaryStatistics()).getSum();
            double expenses = statsByType.getOrDefault(TransactionType.DEBIT, new DoubleSummaryStatistics()).getSum();
            System.out.printf("%s: income=%.2f expenses=%.2f net=%.2f%n",
                    month,
                    income,
                    expenses,
                    income - expenses);
        });
        System.out.println();
    }

    public void generateCustomReport(FinancialDataFrame data) {
        System.out.println("Top Destinations");
        System.out.println("----------------");
        data.summarizeBy(Transaction::destination).entrySet().stream()
                .sorted(Map.Entry.<String, DoubleSummaryStatistics>comparingByValue(
                        (left, right) -> Double.compare(right.getSum(), left.getSum())))
                .limit(5)
                .forEach(entry ->
                        System.out.printf("%-20s total=%.2f count=%d%n",
                                entry.getKey(),
                                entry.getValue().getSum(),
                                entry.getValue().getCount()));
        System.out.println();
    }
}
