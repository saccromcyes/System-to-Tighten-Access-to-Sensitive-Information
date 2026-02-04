package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.util.Map;
import java.util.stream.Collectors;

public class InsightsService {
    public void analyzeRecurringCharges(FinancialDataFrame data) {
        System.out.println("Recurring Charges");
        System.out.println("-----------------");
        Map<String, Long> recurring = data.transactions().stream()
                .filter(transaction -> transaction.type() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(this::signature, Collectors.counting()));

        recurring.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2)
                .forEach(entry -> System.out.printf("%s detected %d times%n",
                        entry.getKey(),
                        entry.getValue()));
        System.out.println();
    }

    private String signature(Transaction transaction) {
        return String.format("%s ($%.2f)", transaction.destination(), transaction.amount());
    }
}
