# CashFlow User Guide & Operational Manual

Welcome to **CashFlow**, an enterprise-grade Liquidity Management, Cash Runway Forecasting, and Multi-Channel Customer Outreach platform built for small business owners and finance teams.

---

## 1. Quick Start & Application Launch

### Launching the Web Application
1. Ensure Java 21+ and Maven are installed.
2. In the project root, start the application:
   ```bash
   mvn spring-boot:run
   ```
3. Open your web browser to:
   ```
   http://localhost:8080/index.html
   ```

---

## 2. Navigation Overview

The application features a responsive sidebar navigation with instant access to 10 key modules:

| Icon / Name | Description | Key Capabilities |
| :--- | :--- | :--- |
| **Dashboard** | Executive Cockpit | Zero-scroll financial health, liquid balance, runway, receivables ratio. |
| **Transactions** | Double-Entry Ledger | Search, sort, and filter transactions by type, category, status, and text. |
| **Receivables** | Aging & Credit Control | Bucketed aging schedule (0-30, 31-60, 61-90, 90+ days), payment delay analysis. |
| **Communication** | Outreach Center | Direct phone dialer, SMS reminders, email statements, and dual-party audit log. |
| **Expenses** | Spend & Burn Rate | Category breakdown, monthly historical spend, scheduled recurring obligations. |
| **Forecast** | Predictive Trajectory | 7, 30, 60, and 90-day forward cash trajectory with safety floor breach markers. |
| **Warnings** | Early Alert Radar | Statistical anomaly detection (e.g. 3x spend spikes) and liquidity breach alerts. |
| **Versions** | Dataset Lineage | Audit trail of uploaded CSVs with side-by-side differential delta comparison. |
| **What-If** | Stress Simulator | Model emergency expenses, revenue drops, or client payment delays. |
| **Settings** | Business & Theming | Set safe cash threshold, contact phone/email, and switch among 5 themes. |

---

## 3. Executive Dashboard (Zero-Scroll Design)

The Dashboard provides immediate visibility into your business's solvency:
- **Liquid Current Cash**: The realized in-bank liquid balance.
- **Expected Inflow vs Upcoming Expenses**: Total collections due against scheduled commitments.
- **Receivables & Overdue Portfolio**: Immediate alerts if debtor accounts are past due.
- **Cash Dynamics Bar Chart**: Instant visual comparison of cash components.
- **Receivables Exposure Doughnut**: Visual breakdown of on-time pending vs delayed overdue invoices.
- **Cash Flow History Curve**: Multi-series trajectory showing cumulative balance and daily cash velocity.

---

## 4. Multi-Channel Customer Communication Hub

Located in the **Communication** page:
1. **Directory View**: Lists all debtor accounts with registered mobile phone numbers, billing emails, total balances, and overdue amounts.
2. **One-Click Phone Call**:
   - Click the phone icon to launch your device's native telephone dialer (`tel:`).
   - Enter call settlement notes in the modal to permanently log payment promises.
3. **SMS Reminders**:
   - Dispatch pre-formatted reminder text with character counter (max 160 chars).
   - Automatically records a twin copy to the shopkeeper's phone for audit verification.
4. **Formal Statement Email**:
   - Send itemized billing statements directly to the client's accounting department.
   - Shopkeeper's email is automatically CC'd for remittance tracking.
5. **Bulk Defaulters Broadcast**:
   - Click **"Alert All Defaulters (Dual-Party)"** to immediately notify all past-due debtors across SMS and email in a single coordinated action.
6. **Audit Ledger**:
   - Switch to the **Communication & Call Audit Trail** tab to view immutable timestamped logs of every call, SMS, and email sent.

---

## 5. Receivables Management & Filtering

- **Status Filtering**:
  - `All Receivables`: Displays all invoices.
  - `Overdue Only`: Strictly filters invoices where `dueDate < Today` or status is `OVERDUE`.
  - `Pending Only`: Strictly filters pending invoices that are not yet past their due date.
- **Instant Search**: Type customer name, invoice ID (`#TXN006`), phone, or email into the search box to filter rows in real time.
- **Aging Analysis**: Visual bar charts categorize unpaid debts by aging brackets to highlight collection priorities.

---

## 6. Forward Cash Forecasting (7, 30, 60, 90 Days)

The **Forecast** module projects your daily closing cash balance forward in time:
- Click **7 Days**, **30 Days**, **60 Days**, or **90 Days** buttons to dynamically switch horizons.
- **Safety Threshold Curve**: Plotted as a horizontal red dashed line representing your Minimum Safe Operating Cash (configured in Settings).
- **Early Breach Warnings**: If projected closing cash drops below the threshold, the date and deficit amount are highlighted.

---

## 7. Dataset Versions & Differential Comparison

- **Multiple Datasets**: Use the topbar dropdown **"Switch Demo Dataset..."** to instantly load:
  - *V1: Baseline Q3 Operations (25 txns)*
  - *V2: Rapid Expansion (30 txns)*
  - *V3: Liquidity Stress Crisis (35 txns)*
- **Calculate Differences**:
  1. Open the **Versions & Changes** page.
  2. Select Baseline Version (e.g. `V1`) and Target Version (e.g. `V2`).
  3. Click **"Calculate Differences"** to view itemized added, modified, and removed transactions with color-coded delta badges.

---

## 8. Themes & Visual Palettes

Customize the visual aesthetic in **Settings > Color Theme & Visual Palette**:
- **Arctic Ledger**: Professional dark navy with ice blue accents (Default).
- **Forest Ledger**: Deep executive green with emerald accents.
- **Copper Desk**: Warm slate charcoal with terracotta amber accents.
- **Ocean Glass**: Deep aquatic teal with vibrant cyan highlights.
- **Paper & Ink**: Clean high-contrast cream accounting ledger style.

Theme choices are immediately applied across all UI components and canvas charts, and persisted to both `localStorage` and the database.
