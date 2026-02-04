package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.time.YearMonth;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public class VisualizationService {
    public void visualizeExpenseTrends(FinancialDataFrame data) {
        System.out.println("Expense Trends");
        System.out.println("--------------");
        Map<YearMonth, DoubleSummaryStatistics> monthlyExpenses = data.transactions().stream()
                .filter(transaction -> transaction.type() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date()),
                        Collectors.summarizingDouble(Transaction::amount)));

        monthlyExpenses.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int barLength = (int) Math.min(40, entry.getValue().getSum() / 50);
                    System.out.printf("%s | %s %.2f%n",
                            entry.getKey(),
                            "#".repeat(Math.max(1, barLength)),
                            entry.getValue().getSum());
                });
        System.out.println();
    }

    public void visualizeTransactionCategories(FinancialDataFrame data) {
        System.out.println("Category Visualization");
        System.out.println("----------------------");
        Map<String, DoubleSummaryStatistics> categoryTotals = data.summarizeByCategory(TransactionType.DEBIT);

        categoryTotals.entrySet().stream()
                .sorted((left, right) -> Double.compare(right.getValue().getSum(), left.getValue().getSum()))
                .forEach(entry -> {
                    int barLength = (int) Math.min(40, entry.getValue().getSum() / 25);
                    System.out.printf("%-15s | %s %.2f%n",
                            entry.getKey(),
                            "*".repeat(Math.max(1, barLength)),
                            entry.getValue().getSum());
                });
        System.out.println();
    }
}
