package com.example.finance.model;

import java.time.LocalDate;

public record Transaction(
        LocalDate date,
        String description,
        String category,
        TransactionType type,
        double amount,
        double balance,
        String destination
) {
    @Override
    public String toString() {
        return String.format(
                "%s | %-15s | %-12s | %-6s | %8.2f | %10.2f | %s",
                date,
                category,
                description,
                type,
                amount,
                balance,
                destination
        );
    }
}
