#!/usr/bin/env python3
from __future__ import annotations

import csv
import os
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


@dataclass(frozen=True)
class Transaction:
    date: datetime
    description: str
    category: str
    tx_type: str
    amount: float
    balance: float
    destination: str


def load_transactions(csv_path: Path) -> list[Transaction]:
    transactions: list[Transaction] = []
    with csv_path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            transactions.append(
                Transaction(
                    date=datetime.strptime(row["date"].strip(), "%Y-%m-%d"),
                    description=row["description"].strip(),
                    category=row["category"].strip(),
                    tx_type=row["type"].strip().upper(),
                    amount=float(row["amount"]),
                    balance=float(row["balance"]),
                    destination=row["destination"].strip(),
                )
            )
    return transactions


def format_currency(value: float, currency: str) -> str:
    symbols = {"USD": "$", "EUR": "€", "GBP": "£"}
    symbol = symbols.get(currency.upper(), f"{currency.upper()} ")
    return f"{symbol}{value:,.2f}"


def build_report(transactions: list[Transaction], currency: str) -> str:
    income_total = sum(t.amount for t in transactions if t.tx_type == "CREDIT")
    expense_total = sum(t.amount for t in transactions if t.tx_type == "DEBIT")
    net = income_total - expense_total

    monthly_expenses: dict[str, float] = defaultdict(float)
    destination_totals: dict[str, float] = defaultdict(float)
    for tx in transactions:
        month = tx.date.strftime("%Y-%m")
        if tx.tx_type == "DEBIT":
            monthly_expenses[month] += tx.amount
            destination_totals[tx.destination] += tx.amount

    sorted_months = sorted(monthly_expenses.items())
    max_expense = max(monthly_expenses.values(), default=1)
    top_destinations = sorted(destination_totals.items(), key=lambda item: item[1], reverse=True)[:5]

    rows = []
    for month, total in sorted_months:
        pct = (total / max_expense) * 100
        rows.append(
            f"<tr><td>{month}</td><td>{format_currency(total, currency)}</td>"
            f"<td><div class='bar'><span style='width:{pct:.0f}%'></span></div></td></tr>"
        )

    destination_rows = [
        f"<tr><td>{destination}</td><td>{format_currency(total, currency)}</td></tr>"
        for destination, total in top_destinations
    ]

    return f"""
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Financial Analysis Preview</title>
  <style>
    body {{ font-family: "Inter", system-ui, sans-serif; background: #0b1120; color: #e2e8f0; margin: 0; }}
    header {{ padding: 32px 40px; background: linear-gradient(135deg, #1e293b, #0b1120); }}
    h1 {{ margin: 0 0 8px; font-size: 30px; }}
    .subtitle {{ color: #94a3b8; margin: 0; }}
    main {{ padding: 32px 40px 48px; display: grid; gap: 24px; }}
    .card {{ background: #111827; border: 1px solid #1f2937; border-radius: 16px; padding: 20px; }}
    .grid {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }}
    .metric {{ font-size: 20px; font-weight: 600; }}
    .label {{ color: #94a3b8; font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em; }}
    table {{ width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 14px; }}
    th, td {{ padding: 8px 6px; border-bottom: 1px solid #1f2937; text-align: left; }}
    .bar {{ height: 8px; background: #1f2937; border-radius: 999px; overflow: hidden; }}
    .bar > span {{ display: block; height: 8px; background: #38bdf8; }}
    .pill {{ display: inline-block; padding: 4px 10px; border-radius: 999px; background: #1f2937; color: #7dd3fc; font-size: 12px; }}
    footer {{ color: #64748b; font-size: 12px; text-align: center; padding: 16px 0 32px; }}
  </style>
</head>
<body>
<header>
  <h1>Financial Analysis Preview</h1>
  <p class="subtitle">Python-powered report with recruiter-ready visuals.</p>
</header>
<main>
  <section class="grid">
    <div class="card">
      <div class="label">Total Income</div>
      <div class="metric">{format_currency(income_total, currency)}</div>
    </div>
    <div class="card">
      <div class="label">Total Expenses</div>
      <div class="metric">{format_currency(expense_total, currency)}</div>
    </div>
    <div class="card">
      <div class="label">Net Cashflow</div>
      <div class="metric">{format_currency(net, currency)}</div>
    </div>
  </section>

  <section class="card">
    <div class="label">Monthly Expenses</div>
    <table>
      <thead><tr><th>Month</th><th>Total</th><th>Trend</th></tr></thead>
      <tbody>
        {"".join(rows)}
      </tbody>
    </table>
  </section>

  <section class="card">
    <div class="label">Top Spending Destinations</div>
    <table>
      <thead><tr><th>Destination</th><th>Total</th></tr></thead>
      <tbody>
        {"".join(destination_rows)}
      </tbody>
    </table>
  </section>

  <section class="card">
    <div class="label">Artifacts</div>
    <p class="subtitle">Preview ready: <span class="pill">report.html</span></p>
  </section>
</main>
<footer>Generated by Python Preview Engine</footer>
</body>
</html>
"""


def main() -> None:
    root_dir = Path(__file__).resolve().parents[1]
    csv_path = root_dir / "src" / "main" / "resources" / "sample" / "financial_transactions.csv"
    output_dir = root_dir / "out"
    output_dir.mkdir(parents=True, exist_ok=True)
    currency = os.environ.get("CURRENCY", "USD")

    report = build_report(load_transactions(csv_path), currency)
    report_path = output_dir / "report.html"
    report_path.write_text(report, encoding="utf-8")
    print(f"Preview ready: {report_path}")


if __name__ == "__main__":
    main()
