package com.example.finance;

import com.example.finance.analysis.FinancialDataFrame;
import com.example.finance.analysis.ForecastService;
import com.example.finance.analysis.InsightsService;
import com.example.finance.analysis.RiskService;
import com.example.finance.analysis.ReportService;
import com.example.finance.analysis.SummaryExportService;
import com.example.finance.analysis.VisualizationService;
import com.example.finance.dataset.HamOrSpamDatasetReader;
import com.example.finance.io.FinancialCsvReader;
import com.example.finance.util.RolesCache;

import java.nio.file.Path;
import java.util.List;

public final class FinancialAnalysisApp {
    private FinancialAnalysisApp() {
    }

    public static void main(String[] args) {
        CliOptions options = CliOptions.parse(args);
        FinancialDataFrame financialData = FinancialCsvReader.readTransactions(options.csvPath());

        System.out.println("Financial Analysis CLI");
        System.out.println("======================");

        ReportService reportService = new ReportService();
        VisualizationService visualizationService = new VisualizationService();
        InsightsService insightsService = new InsightsService();
        RiskService riskService = new RiskService(options.currency());
        ForecastService forecastService = new ForecastService(options.currency());
        SummaryExportService exportService = new SummaryExportService(options.currency());

        reportService.analyzeExpenses(financialData);
        reportService.identifyHighValueTransactions(financialData, options.threshold());
        reportService.categorizeTransactions(financialData);
        reportService.generateMonthlyReport(financialData);
        reportService.generateCustomReport(financialData);

        visualizationService.visualizeExpenseTrends(financialData);
        visualizationService.visualizeTransactionCategories(financialData);
        insightsService.analyzeRecurringCharges(financialData);
        riskService.printRiskSummary(financialData);
        forecastService.printForecast(financialData);

        financialData.displayColumns();
        financialData.displayHead(options.previewLimit());
        financialData.displaySample(options.previewLimit());
        financialData.displayTransactionTypes();
        financialData.displayDestinationCounts();
        financialData.filterHighValueTransactions(options.threshold()).forEach(System.out::println);
        financialData.averageBalanceByDestination().forEach((destination, average) ->
                System.out.printf("Average balance for %-20s : %.2f%n", destination, average));

        List<String> skippedFiles = HamOrSpamDatasetReader.readHamOrSpamDataset(Path.of(options.hamSpamPath()));
        if (!skippedFiles.isEmpty()) {
            System.out.println("Skipped dataset files:");
            skippedFiles.forEach(file -> System.out.println(" - " + file));
        }

        RolesCache rolesCache = new RolesCache(10);
        rolesCache.set("admin", "Administrator Role");
        rolesCache.set("user", "Regular User Role");

        rolesCache.get("admin").ifPresent(role -> System.out.println("Your role is " + role));
        rolesCache.get("user").ifPresent(role -> System.out.println("Your role is " + role));

        exportService.writeSummaryReports(financialData, options.exportDir());
    }
}
