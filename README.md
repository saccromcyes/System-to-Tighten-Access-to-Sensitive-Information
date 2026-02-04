# Financial Analysis CLI

This project is a production-ready Java 17 CLI that analyzes financial transactions with clean, explicit, and efficient code. It includes:

- **CSV ingestion** with validation and fallback to bundled sample data.
- **DataFrame-style operations** (filters, grouping, sampling).
- **Reports and insights** (monthly summaries, category splits, recurring charges).
- **ASCII visualizations** for quick terminal interpretation.
- **Risk & anomaly scoring** using explainable statistics (z-score outliers + category hotspots).
- **Cashflow forecasting** with rolling averages for the next 3 months.
- **Exportable outputs** in Markdown, JSON, and HTML for recruiter-friendly review artifacts.
- **HTML preview dashboard** with a polished, dark-mode report suitable for screenshots.
- **Utility LRU cache** with clear API and documentation.

## Quick Start

```bash
mvn clean package
java -jar target/financial-analysis-1.0.0.jar --csv src/main/resources/sample/financial_transactions.csv --threshold 500
```

### Preview (HTML)

Generate the report and open the HTML preview (Python-powered):

```bash
./scripts/preview.sh
open out/report.html
```

You can also generate the report directly with Python:

```bash
python3 scripts/preview.py
```

## CLI Options

| Option | Description | Default |
| --- | --- | --- |
| `--csv <path>` | Path to a CSV file with transactions | sample dataset |
| `--threshold <amount>` | Threshold for high-value transactions | `500` |
| `--limit <count>` | Limit for preview sections | `5` |
| `--export-dir <path>` | Output directory for summary exports | `out` |
| `--currency <code>` | Currency code (USD/EUR/GBP) | `USD` |
| `--ham-spam-path <path>` | Optional dataset scan path | `path/to/dataset` |

## CSV Schema

The CSV parser expects the following columns:

```
date,description,category,type,amount,balance,destination
```

Example line:

```
2024-01-12,Payment - Coffee,Food & Dining,DEBIT,4.50,2195.30,Coffee Shop
```

## Example Output

```
Monthly Report
-------------
2024-01: income=5200.00 expenses=1320.55 net=3879.45
2024-02: income=5100.00 expenses=1555.90 net=3544.10

Recurring Charges
-----------------
Streaming Service ($12.99) detected 3 times

Risk & Anomaly Insights
-----------------------
Anomaly: Airline            $580.00 (2024-02-23)
Risk hotspot: Housing       total=$5800.00

Cashflow Forecast
-----------------
2024-05 projected expenses: $2075.12
```

## Architecture Snapshot

```
CLI (FinancialAnalysisApp)
 ├─ CSV Reader -> FinancialDataFrame
 ├─ ReportService (summaries)
 ├─ VisualizationService (ASCII charts)
 ├─ InsightsService (recurring charges)
 ├─ RiskService (anomalies + hotspots)
 ├─ ForecastService (3-month outlook)
 └─ SummaryExportService (markdown/json/html)
```

## Notes for Recruiters

This repository demonstrates:

- **Readable, maintainable code** (layered architecture with cohesive classes).
- **Efficiency and explicitness** in data processing.
- **Portfolio-ready artifacts** (summaries exported to `out/summary.md` and `out/summary.json`).
- **Professional-grade CLI behavior** suitable for portfolio review.

---

If you'd like additional export formats (JSON/Parquet), database integration, or a UI, those can be added with minimal changes thanks to the modular design.
