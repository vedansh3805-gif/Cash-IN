# 💰 CashFlow ERP — Small Business Liquidity Management & Cash Runway Early Warning System

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/SQLite-3.45-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite" />
  <img src="https://img.shields.io/badge/Frontend-Vanilla_JS_%26_HTML5_Canvas-E34F26?style=for-the-badge&logo=html5&logoColor=white" alt="HTML5 Canvas" />
  <img src="https://img.shields.io/badge/Tests-52_Passed-success?style=for-the-badge&logo=junit5&logoColor=white" alt="52 Tests" />
  <img src="https://img.shields.io/badge/Dependencies-Zero_CDN_%2F_100%25_Offline-blue?style=for-the-badge" alt="Offline Ready" />
</p>

---

## 📌 Executive Overview

**CashFlow ERP** is an enterprise-grade financial liquidity management platform designed specifically for small businesses, wholesalers, retail chains, and suppliers. 

> **The Core Problem:** Over 82% of small businesses fail not from lack of sales or profitability, but due to **unmanaged cash flow mismatch**—invoices remaining unpaid for 60+ days while operating expenses (payroll, vendor raw materials, rent, utility bills) demand immediate cash outflow.

CashFlow ERP bridges the critical gap between traditional static accounting systems and real-time operational treasury:
1. **Double-Entry Journal Engine**: Real-time ledger tracking liquid cash, accounts receivable, and immediate liabilities.
2. **Predictive Cash Runway Projection**: Forward mathematical simulation (7 to 90 days) warning operators days before bank balances breach safe operating thresholds.
3. **Statistical Anomaly Radar**: Real-time Z-score and 3x multiplier detection catching suspicious billing spikes and rogue disbursements.
4. **Multi-Channel Debt Recovery Hub**: One-click in-app telephone logging, dual-party audit SMS, and formal remittance statement generation.
5. **What-If Scenario Simulator**: Stress-test finances against emergency Capex shocks, customer defaults, and demand contractions before they occur.

---

## 🏛️ System Architecture

CashFlow ERP follows a high-performance, self-contained architecture with zero external runtime dependencies or CDNs.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            PRESENTATION LAYER                               │
│  Zero-CDN Single Page Application • Custom HTML5 Canvas Chart Engine        │
├───────────────────────┬─────────────────────────────┬───────────────────────┤
│ Component 1: Home     │ Component 2: Menu / Catalog │ Component 3: Login    │
│ Executive Dashboard   │ Double-Entry Ledger Table   │ Authentication Overlay│
│ • 8 KPI Metric Cards  │ • 10-Column Filter Table    │ • Quick-Fill Personas │
│ • 3 Canvas Charts     │ • Debounced Search & Export │ • Role Session Access │
└───────────┬───────────┴──────────────┬──────────────┴───────────┬───────────┘
            │                          │                          │
            ▼                          ▼                          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    REST API DISPATCHER (Spring Boot 3.3.5)                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  /api/dashboard    •  /api/transactions  •  /api/receivables                │
│  /api/forecast     •  /api/warnings      •  /api/anomalies                  │
│  /api/scenarios    •  /api/versions      •  /api/communication              │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            CORE BUSINESS SERVICES                           │
├──────────────────────────────────────┬──────────────────────────────────────┤
│ • ForecastService (Runway Engine)    │ • CsvImportService (OpenCSV Parser)  │
│ • AnomalyService (Z-Score & 3x Spike)│ • ReceivablesService (Aging Matrix)  │
│ • ScenarioService (What-If Shocks)   │ • CommunicationService (Audit Log)   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          PERSISTENCE LAYER (ACID)                           │
│  Embedded SQLite 3.45 • HikariCP High-Performance Pooled JDBC Driver        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Key Functional Modules

### 1. Executive Dashboard (The Financial Cockpit)
* **Zero-Scroll Design**: Responsive auto-fit KPI grid engineered for both high-resolution desktop monitors and laptop screens.
* **8 Instant Health Indicators**: Liquid In-Bank Cash, Expected Collections, Upcoming Outflows, Net Period Flow, Receivables Exposure, Overdue Debtors, Forecasted Minimum Cash, and Real-Time Risk Badge (`SAFE`, `WARNING`, `CRITICAL`).
* **HTML5 Canvas Visualizations**:
  * **Cash Dynamics Bar Chart**: Inflows vs. Outflows comparison.
  * **Receivables Exposure Doughnut**: Current vs. 30/60/90+ day overdue exposure.
  * **Liquidity Curve**: Full-width cumulative liquid bank balance over time.

### 2. Double-Entry Transactions Ledger & Catalog
* **Enterprise 10-Column Catalog**: Itemized view with transaction ID, counterparty, category badge, payment flow (IN/OUT), invoice date, due date, amount, and payment status.
* **Instant Filtering & Search**: 300ms debounced search bar matching names, invoice numbers, phones, and emails across all dataset versions.
* **Auditing Modal**: Comprehensive transaction metadata inspection with dual-party contact logs.

### 3. Receivables & Aging Schedule (Debtor Recovery)
* **Aging Buckets**: Automatically segments debtors into `0–30 Days`, `31–60 Days`, `61–90 Days`, and `90+ Days (High Risk)`.
* **Debtor Delay Profiling**: Horizontal bar chart identifying historical chronic defaulters.
* **Contact Quick-Actions**: In-app telephone triggers (`tel:`), pre-filled SMS modals, and email remittal dispatches.

### 4. Multi-Channel Outreach Center
* **Outbound Telephone Logger**: Records phone conversations, call notes, and commitment dates.
* **Dual-Party SMS Reminders**: Formats SMS notifications with transaction IDs and dispatches simultaneous audit logs to both buyer and shopkeeper.
* **Formal Billing Statement Dispatcher**: Generates audit-compliant statement emails with customized subject lines and remittance schedules.

### 5. Forward Cash Runway Forecast
* **Horizon Selection**: 7-day, 30-day, 60-day, and 90-day predictive forward projections.
* **Deficit Breach Marker**: Superimposes business safe-operating cash floors (red-dashed threshold) to visualize upcoming liquidity shortfalls weeks in advance.
* **Daily Closing Cash Table**: Itemized day-by-day cash projection ledger.

### 6. Early Warning Radar & Statistical Anomaly Detector
* **Liquidity Floor Breach Radar**: Mathematical countdown to projected zero-cash or threshold breach date.
* **Z-Score Anomaly Detector**: Identifies abnormal expense spikes deviating $> 3\sigma$ from historical baseline categories.
* **3x Spend Multiplier Alerts**: Flags single transactions exceeding 300% of rolling 30-day category averages.

### 7. What-If Stress Simulator
* **1-Click Preset Scenario Chips**:
  * 🔴 **Client Payment Delay**: Simulates top 3 buyers pushing payments by 30 days.
  * 🔴 **Emergency Capex Shock**: Applies immediate ₹1,00,000 machinery repair expense.
  * 🔴 **Market Demand Contraction**: Models 25% drop in incoming cash flow.
  * 🔴 **Operational Cost Inflation**: Simulates 15% surge in supply-chain disbursements.
* **Multi-Shock Row Builder**: Custom user-defined adjustments applied across specific future dates.
* **Dual-Trajectory Curve**: Compares standard baseline runway against stressed simulation curve.

### 8. Dataset Versioning & Ingestion Lineage
* **13-Column CSV Schema Ingestion**: Powered by OpenCSV with strict phone (E.164) and email regex validation.
* **Automated Differential Comparator**: Calculates delta records (Added, Modified, Removed) between baseline and incoming datasets.
* **Audit Timeline**: Chronological log tracking dataset migrations and administrative adjustments.

### 9. Component 3: Authentication & Security Access
* **Responsive Login Overlay**: Floating glassmorphism authentication card adhering to active theme variables.
* **1-Click Review Personas**:
  * 👤 **Admin**: `admin@cashflow.com` / `password123`
  * 📋 **Auditor**: `auditor@cashflow.com` / `audit2026`
  * 🏪 **Shopkeeper**: `shopkeeper@cashflow.com` / `owner123`
* **Session Management**: Persistent local session tracking with instant topbar **Logout** button.

### 10. Multi-Theme Customizer & Accessibility
* Comes bundled with 5 visual palettes:
  * ❄️ **Arctic Ledger** (Modern Dark Cyan & Slate)
  * 🌲 **Forest Ledger** (Emerald & Deep Charcoal)
  * ☕ **Copper Desk** (Amber, Warm Bronze & Slate)
  * 🌊 **Ocean Glass** (Deep Cobalt Blue & Electric Azure)
  * 📄 **Paper & Ink** (Crisp High-Contrast Editorial Light Mode)

---

## 🚀 Quick Start Guide

### Prerequisites
* **Java Development Kit (JDK)**: Version 21 or higher
* **Apache Maven**: Version 3.8+ (or use bundled Maven wrapper)
* Modern web browser (Chrome, Edge, Safari, Firefox)

### 1. Clone the Repository
```bash
git clone https://github.com/vedansh3805-gif/Cash-IN.git
cd Cash-IN
```

### 2. Build and Run the Server
```bash
mvn spring-boot:run
```

The Spring Boot embedded Tomcat web server will start on port `8080`:
```
Started CashFlowApplication in 0.825 seconds (process running for 0.923)
Tomcat started on port 8080 (http) with context path '/'
```

### 3. Open in Browser
Visit the application at:
```
http://localhost:8080/index.html
```

* Use any of the **Quick-Fill** persona buttons (**Admin**, **Auditor**, or **Shopkeeper**) on the login screen.
* Click **Sign In to Financial Dashboard** to access the system.
* Click **Logout** in the topbar anytime to return to the login interface.

---

## 🧪 Automated Testing & Dry-Run Suite

CashFlow ERP includes a comprehensive test suite covering mathematical models, CSV parsers, multi-channel communications, and permutation workflows:

```bash
mvn test
```

### Test Suite Execution Output:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.cashflow.demo.PermutationCombinationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.cashflow.demo.ComprehensiveDryRunTest
[TEST 1 PASSED] Initial Dataset imported. Valid rows: 10
[TEST 2 PASSED] Dashboard KPIs verified: Current Cash = -45000.0, Risk = CRITICAL
[TEST 3 PASSED] All Transaction filters (Search, Phone, Email, Status, Type) verified!
[TEST 4 PASSED] Receivables and Customer Contact Directory verified!
[TEST 5 PASSED] Outbound call placed & logged with Shopkeeper copy!
[TEST 6 PASSED] Dual-party SMS dispatched!
[TEST 7 PASSED] Formal billing statement email dispatched!
[TEST 8 PASSED] What-If Simulation verified: Stressed Runway matches analytical delta!
[TEST 9 PASSED] Early Warning Radar verified!
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.cashflow.ui.BackendUiServiceTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.cashflow.service.CommunicationServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.cashflow.service.CsvValidationServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] Results:
[INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 📂 Project Structure

```
Cash-IN/
├── pom.xml                                   # Maven dependencies & build definitions
├── .gitignore                                # Excludes binaries, target, and DB artifacts
├── PROJECT_REVIEW_REPORT.md                  # Comprehensive academic review report
├── ARCHITECTURE.md                           # Deep-dive system architecture specification
├── USER_GUIDE.md                             # Step-by-step user operational manual
├── DECISIONS.md                              # Key technical architecture decisions
├── dataset-1-baseline.csv                    # Ingestion fixture 1: Normal operations
├── dataset-2-expansion.csv                   # Ingestion fixture 2: High growth & capex
├── dataset-3-stress.csv                      # Ingestion fixture 3: Severe liquidity deficit
│
└── source/
    ├── main/
    │   ├── java/com/cashflow/
    │   │   ├── CashFlowApplication.java      # Application bootstrap entrypoint
    │   │   ├── config/DatabaseConfig.java    # SQLite & HikariCP datasource setup
    │   │   ├── model/                        # Domain entities (Transaction, Customer, etc.)
    │   │   ├── repository/                   # Spring JDBC query & persistence repositories
    │   │   ├── service/                      # Core business logic, forecast & anomaly math
    │   │   ├── controller/                   # REST API controllers
    │   │   └── ui/                           # Optional JavaFX companion desktop views
    │   │
    │   └── resources/
    │       ├── application.properties        # Server port, database URL, and logging
    │       ├── schema.sql                    # SQLite DDL database table creation
    │       └── static/                       # Frontend web cockpit
    │           ├── index.html                # Single-page application entrypoint
    │           ├── css/                      # Modular CSS styling (layout, components, themes)
    │           └── js/                       # Modular vanilla JavaScript controllers
    │               ├── api.js                # REST client
    │               ├── charts.js             # Pure HTML5 Canvas rendering engine
    │               ├── auth.js               # Component 3 Authentication controller
    │               ├── dashboard.js          # KPI and cockpit controller
    │               ├── transactions.js       # Ledger & catalog table controller
    │               ├── receivables.js        # Aging schedule and debtors controller
    │               ├── communication.js      # Dialer, SMS, and email modals
    │               ├── forecast.js           # Runway calculation and curve chart
    │               ├── whatif.js             # Stress test simulation controller
    │               └── warnings.js           # Radar and anomaly alert controller
    │
    └── test/java/com/cashflow/               # 52 unit, integration & dry-run tests
```

---

## 📊 Sample Datasets

For testing and demonstration, 3 ready-to-use accounting datasets are included in the repository root:
1. `dataset-1-baseline.csv`: Standard operational profile (10 transactions) with healthy liquidity.
2. `dataset-2-expansion.csv`: Expansion scenario (20 transactions) with increased revenues and supplier contracts.
3. `dataset-3-stress.csv`: Liquidity stress test scenario (35 transactions) with overdue debtor defaults and critical negative cash runway.

Import any of these via the **"Import Dataset CSV"** button in the sidebar or directly via the REST API.

---

## 📜 Academic Rubric Alignment

| Review Slide Requirement | Implemented Feature in CashFlow ERP | Status |
|---|---|:---:|
| **Component 1: Home Page** | Executive Dashboard with brand logo, topbar navigation, active session status pill, 8-card responsive KPI grid, and zero-scroll HTML5 Canvas charts. | ✅ Complete |
| **Component 2: Menu / Catalog** | Double-entry Transactions Ledger & Receivables catalog with category badges, amounts, responsive table/card layouts, debounced search, and multi-field filters. | ✅ Complete |
| **Component 3: Login Page** | Dedicated authentication screen with username/password inputs, client-side validation, 1-click evaluation personas, and topbar Logout flow. | ✅ Complete |
| **Permutation & Combinations** | Automated test suite (`PermutationCombinationTest.java`) systematically verifying all filters, date windows, shock presets, and horizon combinations. | ✅ Complete |

---

## 👨‍💻 Author

* **Vedansh Saxena** — [@vedansh3805-gif](https://github.com/vedansh3805-gif)

---

## 📄 License

This project is licensed under the MIT License - see the `LICENSE` file for details.
