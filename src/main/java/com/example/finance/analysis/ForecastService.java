package com.example.finance.analysis;

import com.example.finance.model.TransactionType;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForecastService {
    private final String currency;

    public ForecastService(String currency) {
        this.currency = currency;
    }

    public void printForecast(FinancialDataFrame data) {
        System.out.println("Cashflow Forecast");
        System.out.println("-----------------");

        List<Map.Entry<YearMonth, DoubleSummaryStatistics>> expenseHistory = data
                .summarizeMonthly(TransactionType.DEBIT).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        if (expenseHistory.size() < 2) {
            System.out.println("Not enough data for forecast.");
            System.out.println();
            return;
        }

        double rollingAverage = expenseHistory.stream()
                .mapToDouble(entry -> entry.getValue().getSum())
                .average()
                .orElse(0.0);

        YearMonth lastMonth = expenseHistory.stream()
                .map(Map.Entry::getKey)
                .max(Comparator.naturalOrder())
                .orElse(YearMonth.now());

        for (int i = 1; i <= 3; i++) {
            YearMonth month = lastMonth.plusMonths(i);
            double forecast = rollingAverage * (1 + 0.02 * i);
            System.out.printf("%s projected expenses: %s%.2f%n",
                    month,
                    currencySymbol(),
                    forecast);
        }
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
