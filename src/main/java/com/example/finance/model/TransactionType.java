package com.example.finance.model;

public enum TransactionType {
    CREDIT,
    DEBIT;

    public static TransactionType from(String value) {
        return TransactionType.valueOf(value.trim().toUpperCase());
    }
}
