# CashFlow Early Warning System — User Manual

---

## 1. Introduction

The **Small Business Cash-Flow Early Warning System** is a pure Java desktop application designed to forecast liquidity, alert small business owners of impending cash crunches, detect anomalous spending, and simulate what-if financial stress tests.

---

### 2. Launching the Application

### Option A: Complete Desktop Application (JavaFX + Spring Boot + SQLite)
To run the full desktop application with real database persistence, communication logging, and forecasting engines:
```bash
cd "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN"

# Compile and run test suite (47 automated tests)
mvn test

# Launch the desktop application
mvn javafx:run -Djavafx.mainClass="com.cashflow.ui.CashFlowDesktopApp"
```

### Option B: Standalone Web Application (Browser-Based Dashboard & Outreach Center)
To access the fully-ready, interactive web app from any modern browser:
```bash
cd "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN"

# Launch Spring Boot server
mvn spring-boot:run
```
Once the server reports `Started CashFlowApplication in ... seconds`:
* Open your browser and navigate to: **`http://localhost:8080/index.html`**
* Features 11 responsive views: Dashboard, Transactions, Receivables, Communication, Expenses, Forecast, Warnings, Versions, Timeline, What-If Simulator, Reports, and Settings.

### Option C: Independent Standalone Desktop Frontend (`frontend-only/`)
To run the presentation layer independently with mock services (no database, no backend):
```bash
cd "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/frontend-only"

# Launch the standalone desktop frontend
mvn javafx:run
```

---

## 3. CSV Dataset Format & Contact Specifications

When shopkeepers upload transaction data, the system automatically parses and detects customer contact columns alongside financial fields.

### 3.1 Pre-Loaded Demo Datasets & Quick Scenario Switcher
To test and demonstrate all features immediately without manually preparing a CSV file, the platform includes 3 pre-built enterprise datasets accessible via the **"Switch Demo Dataset..."** dropdown in the top bar:

1. **Demo 1: Baseline Q3 Operations (`dataset-1-baseline.csv`)**:
   - 25 transactions spanning 90 days.
   - Healthy operating margins, standard customer receipts (ABC Ltd, XYZ Corp, Global Tech), payroll, rent, and utility disbursements.
   - Low financial risk with positive cash buffer above the ₹50,000 threshold.
2. **Demo 2: Rapid Expansion (`dataset-2-expansion.csv`)**:
   - 30 transactions representing Version 2.
   - Warehouse lease expansion, ERP multi-site license, and marketing campaign.
   - Demonstrates real version deltas when compared against V1 (5 Added, 3 Modified, 0 Removed, 22 Unchanged).
3. **Demo 3: Liquidity Stress Crisis (`dataset-3-stress.csv`)**:
   - 35 transactions representing Version 3.
   - $3.5\times$ maintenance anomaly (Emergency Transformer Surge Repair ₹1,85,000).
   - 3 severe overdue customer accounts (Retail Mart, Zenith Exports, PQR Industries) totaling ₹4,05,000 in delayed payments.
   - Causes cash floor to breach safety threshold, triggering CRITICAL risk alerts, early warning radar, and multi-channel debtor outreach workflows.

### Standard 13-Column CSV Header:
```csv
transaction_id,transaction_date,transaction_type,category,description,amount,customer,customer_phone,customer_email,supplier,due_date,status,payment_date
```

### Contact Column Definitions:
* **`customer_phone`** (or `buyer_mobile`, `phone`, `mobile`):
  - Buyer's active mobile number.
  - Formats accepted: `+91-98765-10001`, `9876543210`, `(555) 234-5678`.
  - Used for outbound phone calls and instant SMS reminders.
* **`customer_email`** (or `buyer_email`, `email`):
  - Buyer's accounts or billing email address.
  - Example: `billing@abcltd.com`, `accounts@xyzcorp.com`.
  - Used for formal PDF/statement billing emails.
* **Graceful Fallback**: If a transaction does not have phone or email (e.g., general business expenses or walk-in cash sales), leave the field blank (e.g. `120000.0,,,,,2026-06-10...`). The transaction imports cleanly without errors.

---

## 4. Screen-by-Screen Features & Workflows

### 1. Dashboard (Zero-Scroll Executive View)
* **Single-Screen 100vh Layout**: All dashboard metrics, charts, upcoming payment commitments, and active risk radar fit completely within the viewport without vertical scrolling.
* **8-Metric Compact KPI Strip**: Displays Current Cash, Expected Inflow, Upcoming Expenses, Net Cash Flow, Pending Receivables, Overdue Receivables, Forecasted Minimum Cash, and Risk Assessment in a high-density top ribbon.
* **Cash Dynamics Bar Chart**: Visualizes cash dynamics comparing Current Cash, Expected Inflow, Upcoming Expenses, and Net Flow side-by-side using high-contrast bars.
* **Receivables Risk Exposure Doughnut Chart**: Interactive doughnut chart highlighting On-Time vs Overdue invoice exposure with centered status metrics.
* **Cash Flow Liquidity Line Chart**: Interactive multi-series canvas chart plotting cumulative liquidity balance alongside daily net inflow/outflow bars.
* **Commitments & Alerts Panels**: Upcoming payments and active warnings/recent changes scroll within their respective cards without causing page-level scrollbars.
* **100% Offline Canvas Engine**: Powered by `EnterpriseChart` (`charts.js`) with zero external network or CDN dependencies, HiDPI Retina support, and floating tooltips.

### 2. Transactions
* **Table View**: Browse all imported transactions with sorting and right-aligned currency amounts.
* **Contact Information**: View customer mobile numbers and email addresses directly in the ledger table.
* **Real-time Filters**: Search by keyword, customer name, phone number, filter by Type (`INCOME`/`EXPENSE`), and filter by Status (`PAID`/`PENDING`/`OVERDUE`).

### 3. Forecast
* **Multi-Horizon Trajectory (7, 30, 60, 90 Days)**: Dynamic day-by-day cash projection updating in real-time. Each horizon toggle reflects distinct cash inflows, payroll disbursements, recurring leases, and customer payment maturities:
  - **7 Days**: Immediate 1-week cash movement and overdue debt recovery.
  - **30 Days**: Month 1 payroll, office rent, and scheduled invoice collections.
  - **60 Days**: Month 2 expansion, raw material reorders, and customer payments.
  - **90 Days**: Full quarterly trajectory with quarterly tax and supplier reconciliations.
* **Dashed Safety Threshold**: Red dashed safety threshold line dynamically plots the minimum cash floor across the entire forecast timeline.
* **Summary Badges**: Dynamically updates Horizon period, Minimum Projected Cash Floor, Estimated Ending Balance, and Safety Buffer metrics for the chosen horizon.

### 4. Warnings & Anomalies
* **Early Warning Radar**: Alerts for minimum cash breaches, upcoming large expenses, and overdue customer collections.
* **Actionable Defaulter Resolution**: When a delay warning is flagged, click the **"Contact Defaulter"** button directly on the warning card to jump straight into communication outreach.
* **Anomaly Detector**: Flags transactions exceeding $3\times$ average category spending.

### 5. Receivables & Multi-Channel Defaulter Outreach
The Receivables screen is the central operational hub for managing customer collections and handling overdue accounts:

* **Buyer Contact Columns**:
  - The table prominently displays **Buyer Phone** and **Buyer Email** for every pending invoice.
  - Invoices are color-coded: Amber for `PENDING` and Rose/Red for `OVERDUE` or `HIGH-RISK DELAY`.
  - Aging buckets are calculated relative to the reference period (`Current`, `1-30 Days`, `31-60 Days`, `61-90 Days`, `90+ Days`).
* **Outbound Outreach Actions**:
  Select any invoice row to activate communication actions:
  1. **Call (`Call`)**:
     - Opens the **Call Customer** modal displaying the buyer's telephone number, total overdue amount, and days late.
     - Provides caller guidance points for the shopkeeper.
     - Clicking "Dial Now" connects the call and immediately records a `CONNECTED` event in the audit log.
  2. **SMS Notice (`SMS`)**:
     - Opens the **Send SMS Reminder** dialog with a pre-formatted message:
       `"Payment Reminder: Invoice TXN006 for ₹75,000 was due on 2026-06-30. Please settle immediately."`
     - Allows customized text before dispatch.
     - Logs real-time dispatch with delivery status.
  3. **Email Notice (`Email`)**:
     - Opens the **Send Billing Email** dialog with customer email pre-filled.
     - Generates a formal invoice notification including amount, due date, invoice ID, and payment remittance details.
  4. **Dual-Party Broadcast (`Notify Both`)**:
     - **Core Feature**: Transmits outreach simultaneously to **both ends of the party**:
       - Dispatches the collection reminder to the **Buyer**.
       - Sends a confirmation verification copy to the **Shopkeeper's registered mobile and email** for internal bookkeeping and proof of notification.
  5. **Bulk Defaulters Outreach (`Alert All Defaulters`)**:
     - Click the top-bar button to instantly scan the entire ledger for overdue buyers and dispatch multi-channel reminders to all delinquent parties in one batch.
  6. **Communication Audit History (`View Audit Trail`)**:
     - Click to open the comprehensive outreach ledger showing every Call, SMS, and Email dispatched, complete with exact timestamps, target contact details, recipient party, and delivery confirmation.

### 5.1 Dedicated Communication & Multi-Channel Outreach Page
The standalone **Communication Page** (accessible from the sidebar on both Web and Desktop) offers an all-in-one command center for customer relations and debt collection:
* **Contact Directory**: Displays every debtor with their registered **Mobile Number** and **Email Address**.
* **Click-to-Call & Click-to-Email**: Direct links trigger system dialer (`tel:`) or email client (`mailto:`) instantly.
* **Filter & Search**: Real-time search across customer names, phone numbers, email addresses, and invoice IDs.
* **Status Filter**: View all accounts, overdue defaulters only, or pending invoices only.
* **Dual-View Tabs**:
  1. *Customer Directory & Actions*: Initiate individual Calls, dispatch SMS, send Email statements, or trigger 1-click dual-party alerts.
  2. *Live Audit Trail Ledger*: Full historical record of all past communications, status codes, recipient endpoints, and timestamps.
* **Bulk Alert All Defaulters**: Dispatches batch notifications across all overdue accounts with a single click.

### 6. Expenses & Recurring Payments
* Categorized breakdown of historical business spending rendered via an interactive Doughnut chart.
* Multi-month forward projection bar chart derived from active recurring payments.
* Registry of fixed recurring obligations (e.g., Office Rent, Payroll, Software Subscriptions) with in-app creation and deletion.

### 7. Versions & Differential Version Comparison
* Track immutable dataset versions created with every CSV upload.
* **Differential Comparison Engine**: Select any two arbitrary versions ($v_1$ and $v_2$) to compute exact transaction deltas:
  - **Summary Counters**: Displays exact counts of `Added`, `Modified`, `Removed`, and `Unchanged` records.
  - **Comparative Bar Chart**: Visualizes distribution of change types between selected versions.
  - **Granular Change Ledger**: Lists line-by-line field mutations (e.g., changes to Amount, Due Date, or Status) with old and new values.

### 8. What-If Simulator
* Test hypothetical shocks (e.g., customer payment delays or unexpected equipment expenses) and inspect the simulated cash line against the baseline forecast without corrupting actual data.


### 9. Upload CSV
* Browse and ingest CSV files containing business records (`sample-data.csv`, `sample-data-v2.csv`). Automatically triggers recalculation of forecasts, warnings, and version diffs.
* Clear column guide explains the optional contact headers (`customer_phone`, `customer_email`).

### 10. Reports & Export
* Export consolidated ledger records to a clean CSV file on disk.

### 11. Settings & Shopkeeper Contact Profile
* **Shopkeeper Contact Profile**:
  - Configure the **Shopkeeper Mobile Number** (e.g., `+91-98765-00000`).
  - Configure the **Shopkeeper Email Address** (e.g., `owner@cashflowbiz.com`).
  - These contact details are used as the sender origin and receive copies during **Dual-Party** notifications.
* **Visual Themes**: Switch dynamically between 5 visual themes:
  * **Arctic Ledger** (Deep Navy / Ice Blue)
  * **Forest Ledger** (Deep Forest / Mint)
  * **Copper Desk** (Warm Charcoal / Terracotta)
  * **Ocean Glass** (Deep Ocean / Teal)
  * **Paper & Ink** (High-Contrast Clean Light)
* **Risk Parameters**: Configure minimum cash safety thresholds and forecast horizons.
