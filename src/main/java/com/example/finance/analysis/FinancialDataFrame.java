package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FinancialDataFrame {
    private final List<Transaction> transactions;

    public FinancialDataFrame(List<Transaction> transactions) {
        this.transactions = new ArrayList<>(transactions);
    }

    public List<Transaction> transactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void displayColumns() {
        System.out.println("Columns: date, description, category, type, amount, balance, destination");
    }

    public void displayHead(int limit) {
        System.out.println("Head (" + limit + " rows)");
        transactions.stream().limit(limit).forEach(System.out::println);
    }

    public void displaySample(int limit) {
        System.out.println("Sample (" + limit + " rows)");
        Random random = new Random(42);
        transactions.stream()
                .skip(random.nextInt(Math.max(1, transactions.size())))
                .limit(limit)
                .forEach(System.out::println);
    }

    public void displayTransactionTypes() {
        System.out.println("Transaction Types:");
        transactions.stream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()))
                .forEach((type, count) -> System.out.println(type + ": " + count));
    }

    public void displayDestinationCounts() {
        System.out.println("Destination Counts:");
        transactions.stream()
                .collect(Collectors.groupingBy(Transaction::destination, Collectors.counting()))
                .forEach((destination, count) -> System.out.println(destination + ": " + count));
    }

    public List<Transaction> filterHighValueTransactions(double threshold) {
        System.out.println("High-value Transactions (threshold: " + threshold + ")");
        return transactions.stream()
                .filter(transaction -> transaction.amount() >= threshold)
                .toList();
    }

    public Map<String, Double> averageBalanceByDestination() {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::destination,
                        Collectors.averagingDouble(Transaction::balance)));
    }

    public DoubleSummaryStatistics summarize(TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .collect(Collectors.summarizingDouble(Transaction::amount));
    }

    public Map<YearMonth, DoubleSummaryStatistics> summarizeMonthly(TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date()),
                        Collectors.summarizingDouble(Transaction::amount)));
    }

    public Map<YearMonth, List<Transaction>> transactionsByMonth() {
        return transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date())));
    }

    public Map<String, DoubleSummaryStatistics> summarizeByCategory(TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.type() == type)
                .collect(Collectors.groupingBy(Transaction::category,
                        Collectors.summarizingDouble(Transaction::amount)));
    }

    public <T> Map<T, DoubleSummaryStatistics> summarizeBy(Function<Transaction, T> classifier) {
        return transactions.stream()
                .collect(Collectors.groupingBy(classifier,
                        Collectors.summarizingDouble(Transaction::amount)));
    }
}
