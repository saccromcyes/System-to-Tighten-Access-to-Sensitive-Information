package com.example.finance.analysis;

import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

public class SummaryExportService {
    private final String currency;

    public SummaryExportService(String currency) {
        this.currency = currency;
    }

    public void writeSummaryReports(FinancialDataFrame data, Path outputDir) {
        try {
            Files.createDirectories(outputDir);
            writeMarkdownSummary(data, outputDir.resolve("summary.md"));
            writeJsonSummary(data, outputDir.resolve("summary.json"));
            writeHtmlSummary(data, outputDir.resolve("report.html"));
            System.out.println("Exported summaries to " + outputDir.toAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Failed to export summaries: " + ex.getMessage());
        }
    }

    private void writeMarkdownSummary(FinancialDataFrame data, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("# Financial Summary\n\n");
        builder.append("## Monthly Totals\n\n");
        Map<YearMonth, Map<TransactionType, DoubleSummaryStatistics>> monthly = data.transactions().stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date()),
                        Collectors.groupingBy(Transaction::type,
                                Collectors.summarizingDouble(Transaction::amount))));

        monthly.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    YearMonth month = entry.getKey();
                    Map<TransactionType, DoubleSummaryStatistics> statsByType = entry.getValue();
            double income = statsByType.getOrDefault(TransactionType.CREDIT, new DoubleSummaryStatistics()).getSum();
            double expenses = statsByType.getOrDefault(TransactionType.DEBIT, new DoubleSummaryStatistics()).getSum();
            builder.append(String.format("- %s: income=%s%.2f expenses=%s%.2f net=%s%.2f%n",
                    month,
                    currencySymbol(),
                    income,
                    currencySymbol(),
                    expenses,
                    currencySymbol(),
                    income - expenses));
        });

        builder.append("\n## Top Destinations\n\n");
        data.summarizeBy(Transaction::destination).entrySet().stream()
                .sorted(Map.Entry.<String, DoubleSummaryStatistics>comparingByValue(
                        (left, right) -> Double.compare(right.getSum(), left.getSum())))
                .limit(5)
                .forEach(entry ->
                        builder.append(String.format("- %s: %s%.2f%n",
                                entry.getKey(),
                                currencySymbol(),
                                entry.getValue().getSum())));

        Files.writeString(path, builder.toString());
    }

    private void writeJsonSummary(FinancialDataFrame data, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"monthlyTotals\": [\n");
        Map<YearMonth, Map<TransactionType, DoubleSummaryStatistics>> monthly = data.transactions().stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.date()),
                        Collectors.groupingBy(Transaction::type,
                                Collectors.summarizingDouble(Transaction::amount))));

        var sortedMonthly = monthly.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        for (int i = 0; i < sortedMonthly.size(); i++) {
            Map.Entry<YearMonth, Map<TransactionType, DoubleSummaryStatistics>> entry = sortedMonthly.get(i);
            double income = entry.getValue().getOrDefault(TransactionType.CREDIT, new DoubleSummaryStatistics()).getSum();
            double expenses = entry.getValue().getOrDefault(TransactionType.DEBIT, new DoubleSummaryStatistics()).getSum();
            builder.append(String.format(
                    "    {\"month\": \"%s\", \"income\": %.2f, \"expenses\": %.2f, \"net\": %.2f}%s%n",
                    entry.getKey(),
                    income,
                    expenses,
                    income - expenses,
                    i == sortedMonthly.size() - 1 ? "" : ","));
        }
        builder.append("  ],\n");
        builder.append("  \"currency\": \"").append(currency.toUpperCase()).append("\"\n");
        builder.append("}\n");

        Files.writeString(path, builder.toString());
    }

    private void writeHtmlSummary(FinancialDataFrame data, Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Financial Analysis Preview</title>
                  <style>
                    body { font-family: "Inter", system-ui, sans-serif; background: #0f172a; color: #e2e8f0; margin: 0; }
                    header { padding: 32px 40px; background: linear-gradient(135deg, #1e293b, #0f172a); }
                    h1 { margin: 0 0 8px; font-size: 28px; }
                    .subtitle { color: #94a3b8; margin: 0; }
                    main { padding: 32px 40px 48px; display: grid; gap: 24px; }
                    .card { background: #111827; border: 1px solid #1f2937; border-radius: 16px; padding: 20px; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }
                    .metric { font-size: 20px; font-weight: 600; }
                    .label { color: #94a3b8; font-size: 13px; text-transform: uppercase; letter-spacing: 0.08em; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 14px; }
                    th, td { padding: 8px 6px; border-bottom: 1px solid #1f2937; text-align: left; }
                    .bar { height: 8px; background: #1f2937; border-radius: 999px; overflow: hidden; }
                    .bar > span { display: block; height: 8px; background: #38bdf8; }
                    .pill { display: inline-block; padding: 4px 10px; border-radius: 999px; background: #1f2937; color: #7dd3fc; font-size: 12px; }
                    footer { color: #64748b; font-size: 12px; text-align: center; padding: 16px 0 32px; }
                  </style>
                </head>
                <body>
                <header>
                  <h1>Financial Analysis Preview</h1>
                  <p class="subtitle">Generated report with summaries, trends, and recruiter-ready insights.</p>
                </header>
                <main>
                """);

        DoubleSummaryStatistics expenses = data.summarize(TransactionType.DEBIT);
        DoubleSummaryStatistics income = data.summarize(TransactionType.CREDIT);
        builder.append("""
                <section class="grid">
                  <div class="card">
                    <div class="label">Total Income</div>
                    <div class="metric">""").append(currencySymbol()).append(String.format("%.2f", income.getSum())).append("""
                    </div>
                  </div>
                  <div class="card">
                    <div class="label">Total Expenses</div>
                    <div class="metric">""").append(currencySymbol()).append(String.format("%.2f", expenses.getSum())).append("""
                    </div>
                  </div>
                  <div class="card">
                    <div class="label">Net Cashflow</div>
                    <div class="metric">""").append(currencySymbol()).append(String.format("%.2f", income.getSum() - expenses.getSum())).append("""
                    </div>
                  </div>
                </section>
                """);

        builder.append("""
                <section class="card">
                  <div class="label">Monthly Expenses</div>
                  <table>
                    <thead><tr><th>Month</th><th>Total</th><th>Trend</th></tr></thead>
                    <tbody>
                """);
        Map<YearMonth, DoubleSummaryStatistics> monthlyExpenses = data.summarizeMonthly(TransactionType.DEBIT);
        double maxExpense = monthlyExpenses.values().stream()
                .mapToDouble(DoubleSummaryStatistics::getSum)
                .max()
                .orElse(1.0);

        monthlyExpenses.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    double total = entry.getValue().getSum();
                    double pct = (total / maxExpense) * 100;
                    builder.append("<tr><td>")
                            .append(entry.getKey())
                            .append("</td><td>")
                            .append(currencySymbol())
                            .append(String.format("%.2f", total))
                            .append("</td><td><div class=\"bar\"><span style=\"width:")
                            .append(String.format("%.0f", pct))
                            .append("%\"></span></div></td></tr>");
                });
        builder.append("""
                    </tbody>
                  </table>
                </section>
                """);

        builder.append("""
                <section class="card">
                  <div class="label">Top Spending Destinations</div>
                  <table>
                    <thead><tr><th>Destination</th><th>Total</th></tr></thead>
                    <tbody>
                """);
        data.summarizeBy(Transaction::destination).entrySet().stream()
                .sorted(Map.Entry.<String, DoubleSummaryStatistics>comparingByValue(
                        (left, right) -> Double.compare(right.getSum(), left.getSum())))
                .limit(5)
                .forEach(entry -> builder.append("<tr><td>")
                        .append(entry.getKey())
                        .append("</td><td>")
                        .append(currencySymbol())
                        .append(String.format("%.2f", entry.getValue().getSum()))
                        .append("</td></tr>"));
        builder.append("""
                    </tbody>
                  </table>
                </section>
                """);

        builder.append("""
                <section class="card">
                  <div class="label">Preview artifacts</div>
                  <p class="subtitle">Exported files: <span class="pill">summary.md</span> <span class="pill">summary.json</span> <span class="pill">report.html</span></p>
                </section>
                </main>
                <footer>Generated by Financial Analysis CLI</footer>
                </body>
                </html>
                """);

        Files.writeString(path, builder.toString());
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
