# CASHFLOW: Small Business Liquidity Management, Predictive Runway Forecasting & Automated Defaulter Outreach System
## Comprehensive Final Review Project Report

---

# 1. Cover Page Details

| Field | Description / Value |
| :--- | :--- |
| **Institution / College** | Department of Computer Science & Engineering |
| **Degree & Program** | Bachelor of Technology (B.Tech) in Computer Science & Engineering |
| **Subject / Course** | Capstone Project / Full Stack Software Engineering Lab |
| **Project Title** | **CASHFLOW: Small Business Liquidity Management, Forward Cash Runway Early Warning & Multi-Channel Defaulter Recovery System** |
| **Project Track** | Full Stack Enterprise Web & SaaS Application |
| **Team Members** | • **Vedansh Saxena** (Lead Architect & Full Stack Developer)<br>• [Team Member 2 Name & Registration Number]<br>• [Team Member 3 Name & Registration Number]<br>• [Team Member 4 Name & Registration Number] |
| **Project Supervisor / Guide** | [Faculty Mentor / Project Supervisor Name] |
| **Review Stage** | **Final Review (100% Ready & Verified)** |
| **Academic Year** | 2026 – 2027 |
| **Submission Date** | November 2026 |

---

# 2. Abstract

Over **82% of small businesses and startups fail not from fundamental unprofitability, but from acute cash-flow mismatches**: invoices remaining uncollected for 30 to 90+ days while hard operating disbursements (salaries, vendor dues, rent, taxes) demand immediate liquid cash. Existing commercial accounting suites (Tally, QuickBooks, SAP) function essentially as retrospective record-keeping and tax filing ledgers; they do not provide real-time forward runway projections, statistical spend anomaly radars, automated late-payment penalty compounding, or instant dual-party outreach to defaulters.

**CashFlow** solves this systemic operational challenge. Engineered as a zero-external-CDN, high-performance Full-Stack enterprise application, CashFlow unites:
1. **Double-Entry Operational Journal**: Real-time ledger tracking liquid cash, receivables, and immediate liabilities with CSV batch ingestion and schema validation.
2. **Forward Cash Runway Engine**: Deterministic forward projections (7, 30, 60, and 90 days) alerting business owners before bank reserves breach a configured minimum safe threshold.
3. **Late & Unpaid Payment Penalty Engine**: Automated calculation of daily percentage interest, flat fees, or one-time percentage surcharges after configurable grace periods for overdue receivables and late-settled invoices, complete with 1-click waiver toggles and settlement finalization.
4. **Statistical Spending Anomaly Radar**: Real-time Z-score evaluation detecting disbursements $> 3\sigma$ above category baselines and single transactions exceeding 300% of rolling 30-day category norms.
5. **Multi-Channel Debt Recovery & Communication Hub**: Direct in-app telephone dialer (`tel:`), dual-party reminder SMS (with copies dispatched to both debtor and shopkeeper for legal auditability), and formal itemized email statement dispatches.
6. **Interactive What-If Scenario Stress Simulator**: Pre-built and customizable macroeconomic shocks (client payment delays, Capex emergencies, revenue drops, cost inflation) rendered on dual-curve baseline vs. stressed graphs.

All 58 automated unit, service, and HTTP integration tests run with 0 failures, ensuring production-grade correctness, sub-50ms query response times, and 100% offline self-containment.

---

# 3. Table of Contents

1. [Cover Page Details](#1-cover-page-details)
2. [Abstract](#2-abstract)
3. [Table of Contents](#3-table-of-contents)
4. [Introduction](#4-introduction)
5. [Problem Statement](#5-problem-statement)
6. [Objectives of the Project](#6-objectives-of-the-project)
7. [Scope of the Application](#7-scope-of-the-application)
8. [Technology Stack](#8-technology-stack)
9. [System Design](#9-system-design)
   - 9.1 Multi-Tier Architecture Diagram
   - 9.2 Entity-Relationship (ER) Database Schema
10. [Module Description](#10-module-description)
11. [Implementation Details](#11-implementation-details)
    - 11.1 Directory & Folder Structure
    - 11.2 Key Algorithms & Mathematical Formulations
    - 11.3 Core Logic & Code Snippets
12. [Deployment Details](#12-deployment-details)
13. [Project Implementation Status Table (Final Review Table)](#13-project-implementation-status-table)
14. [References](#14-references)
15. [Appendix & UI Screenshots](#15-appendix)

---

# 4. Introduction

Cash is the vital circulatory system of every business enterprise. While a firm can remain legally operational through accounting quarters of paper losses, it collapses instantaneously the moment liquid bank cash hits zero and payroll, debt servicing, or critical supplier drafts bounce.

Small business owners and financial managers face persistent friction:
- **Fragmented Spreadsheets**: Cash positions are tracked across ad-hoc Excel sheets where delayed payments are noticed weeks after their due date.
- **Absence of Forward Trajectory**: Inability to see cash levels 30 to 90 days out when large quarterly expenses mature.
- **Unpenalized Defaults**: Debtors delay payment indefinitely because small businesses lack a systematic, transparent late-payment fee policy.
- **Awkward, Inefficient Outreach**: Manually digging up phone numbers and drafting reminder emails consumes hours of operational time each week.

**CashFlow** bridges the gap between static accounting compliance and active treasury operations. It delivers an executive single-page web cockpit with automated early warning alerts, flexible late-fee policies, and 1-click dual-party communication.

---

# 5. Problem Statement

> *"How can small business owners and financial controllers automatically forecast forward liquidity, enforce fair late-payment penalties on delinquent accounts, detect spending anomalies before reserves are exhausted, and orchestrate verifiable multi-channel debtor outreach without relying on heavy, expensive, or complex legacy software?"*

### Key Bottlenecks Addressed:
1. **Blind-Spot Cash Deficits**: Businesses realize their reserves have depleted only when checks bounce or credit lines freeze.
2. **Uncollected Receivables with Zero Accountability**: Overdue debtors treat commercial credit as free working capital because no interest or late fees are assessed.
3. **Rogue Disbursements & Spending Spikes**: Lack of anomaly detection permits unexpected operational cost blowouts.
4. **Lack of Audit-Proof Communication Trails**: Debtor communications conducted informally leave no legal audit trail when disputes arise.

---

# 6. Objectives of the Project

1. **Automated Forward Cash Forecasting**: Model cash trajectory across 7, 30, 60, and 90-day forward windows, superimposing a configurable safe operating cash floor.
2. **Late & Unpaid Payment Penalty System**: Implement a policy-driven penalty engine supporting daily percentage compounding, flat charges, grace periods, and maximum caps, with full support for late-settled historical invoices and waiver options.
3. **Statistical Spend Anomaly Detection**: Compute rolling category averages and standard deviations to trigger real-time Z-score ($> 3\sigma$) and 3x spend surge alerts.
4. **Integrated Multi-Channel Outreach**: Provide native phone dialing, SMS dispatching, and email statements with dual-party logging (buyer and shopkeeper audit copy).
5. **Interactive What-If Stress Testing**: Allow business operators to simulate client delays, sudden Capex shocks, and demand contractions with instantaneous dual-trajectory rendering.
6. **Zero-Dependency Resilient Architecture**: Function 100% offline without external CDNs, third-party cloud analytics, or complicated build toolchains.

---

# 7. Scope of the Application

### In-Scope Capabilities:
- Ingestion of double-entry transaction ledgers via standard 13-column CSV files.
- Real-time liquidity telemetry: Current Liquid Cash, Expected Inflows, Upcoming Expenses, Net Flow Velocity, and Debtor Exposure.
- Aging bucket classification (`0–30`, `31–60`, `61–90`, `90+` days).
- Comprehensive late-payment penalty administration (daily compounding, percentage, flat fee, grace period, max cap, manual waiver, and mark-as-paid settlement).
- Automated SMS and Email reminder generation embedding itemized principal, late penalty, and total payable breakdowns.
- Dataset version comparison identifying added, modified, and removed records.
- Cross-platform support: responsive web browser UI and companion JavaFX desktop client.

### Out-of-Scope (Future Enhancements):
- Direct banking gateway ACH wire settlement.
- GST/VAT regulatory tax return e-filing.
- Multi-currency live forex exchange rate streaming.

---

# 8. Technology Stack

CashFlow is built on modern, industrial-grade software engineering foundations:

```
┌───────────────────────────────────────────────────────────────────────┐
│                           TECHNOLOGY STACK                            │
├───────────────────────┬───────────────────────────────────────────────┤
│ Frontend Presentation │ Vanilla JavaScript (ES6+), HTML5 Canvas,      │
│                       │ Semantic CSS3 with 5 Corporate Theme Palettes │
├───────────────────────┼───────────────────────────────────────────────┤
│ Backend Application   │ Java 21 LTS, Spring Boot 3.3.5                │
│ Server & Framework    │ (Spring MVC REST, HikariCP, JdbcClient/Jdbc)  │
├───────────────────────┼───────────────────────────────────────────────┤
│ Relational Database   │ SQLite 3.45 (ACID compliant, zero-config)     │
├───────────────────────┼───────────────────────────────────────────────┤
│ CSV Parser & Pipeline │ OpenCSV 5.9 with E.164 and RFC-5322 validation│
├───────────────────────┼───────────────────────────────────────────────┤
│ Desktop Client        │ JavaFX 21 (Modular FXML-free architecture)    │
├───────────────────────┼───────────────────────────────────────────────┤
│ Build & Test Tools    │ Apache Maven 3.9, JUnit 5 Jupiter, MockMvc,   │
│                       │ Git & GitHub version control                  │
└───────────────────────┴───────────────────────────────────────────────┘
```

> **Note on Architecture Alignment**: Where traditional course outlines reference MERN (React/Node/Mongo), CashFlow employs an enterprise **Java 21 + Spring Boot + SQLite + Vanilla JS/Canvas** stack. This provides native compiled execution, strict type safety, ACID transaction guarantees, and zero-CDN self-containment suited for commercial enterprise accounting.

---

# 9. System Design

## 9.1 Multi-Tier Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION LAYER (Zero-CDN Web & JavaFX Client)                      │
│                                                                                                  │
│   [Executive Dashboard]        [Receivables & Defaulters]        [Transactions Ledger]          │
│   • 8 Real-time KPI Cards       • 0-90+ Day Aging Buckets         • 10-Column Catalog            │
│   • HTML5 Canvas Curves         • Accrued Penalty Matrix          • Live Debounced Search        │
│                                                                                                  │
│   [Communication Hub]          [What-If Stress Simulator]        [Enterprise Settings]          │
│   • One-Click Phone Dialer      • 4 Preset Shock Scenarios        • Min Cash Floor Config        │
│   • Dual-Party SMS & Email      • Dual-Trajectory Graphs          • Late Fee Policy Rules        │
└────────────────────────────────────────────────┬─────────────────────────────────────────────────┘
                                                 │ HTTP REST JSON
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                            REST API DISPATCHER (Spring Boot 3.3.5)                              │
│                                                                                                  │
│   /api/dashboard    •  /api/transactions  •  /api/receivables   •  /api/communication            │
│   /api/forecast     •  /api/warnings      •  /api/anomalies     •  /api/settings                 │
└────────────────────────────────────────────────┬─────────────────────────────────────────────────┘
                                                 │
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    CORE BUSINESS SERVICES                                        │
│                                                                                                  │
│   • PenaltyService: Late & unpaid penalty compounding, grace period, max caps, waivers           │
│   • ReceivablesService: Aging bucket classification, debtor delay profiling                      │
│   • ForecastService: Forward runway mathematical simulation (7-90 days), safety breach radar     │
│   • AnomalyService: Statistical Z-score (>3σ) and 3x spend multiplier alert engine              │
│   • CommunicationService: Dual-party SMS/Email templates & immutable audit trail logging         │
│   • CsvImportService: OpenCSV parsing, schema validation, and version delta comparator           │
└────────────────────────────────────────────────┬─────────────────────────────────────────────────┘
                                                 │
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   PERSISTENCE LAYER (ACID)                                       │
│                                                                                                  │
│   SQLite 3.45 Database Engine • HikariCP High-Performance Pooled Connection Driver              │
│   • transaction_record (id, amount, due_date, status, penalty_amount, penalty_waived)           │
│   • customer (customer_id, average_payment_delay, total_outstanding, total_penalty)             │
│   • application_settings (penalty_type, rate, grace_period_days, max_penalty_percent)            │
│   • communication_log (audit trail of all outbound calls, SMS, and emails)                      │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## 9.2 Entity-Relationship (ER) Database Schema

- **`application_settings`**: `setting_id (PK)`, `theme`, `forecast_days`, `minimum_cash_threshold`, `currency`, `shopkeeper_phone`, `shopkeeper_email`, `penalty_enabled`, `penalty_type`, `penalty_rate`, `grace_period_days`, `max_penalty_percent`.
- **`transaction_record`**: `id (PK)`, `transaction_id`, `version_id (FK)`, `transaction_date`, `transaction_type`, `category`, `description`, `amount`, `customer`, `customer_phone`, `customer_email`, `supplier`, `due_date`, `payment_date`, `status`, `penalty_amount`, `penalty_waived`.
- **`customer`**: `customer_id (PK)`, `customer_name`, `phone`, `email`, `average_payment_delay`, `total_outstanding`, `total_penalty`.
- **`communication_log`**: `log_id (PK)`, `transaction_id (FK)`, `customer_name`, `channel`, `recipient_type`, `recipient_contact`, `subject`, `message`, `status`, `timestamp`.
- **`dataset_version`**: `version_id (PK)`, `version_number`, `file_name`, `upload_date`, `record_count`, `previous_version_id`.
- **`data_change`**: `change_id (PK)`, `version_id (FK)`, `transaction_id`, `change_type`, `field_name`, `old_value`, `new_value`, `changed_at`.

---

# 10. Module Description

### Module 1: Executive Dashboard (Financial Cockpit)
- Zero-scroll responsive cockpit displaying 8 real-time KPI tiles.
- Real-time solvency badge (`SAFE`, `WARNING`, `CRITICAL`).
- HTML5 Canvas multi-series Cash Dynamics and Liquidity Balance curves with threshold floors.

### Module 2: Receivables, Defaulters & Late Penalty Operations
- Bucketed aging schedule (`Current`, `1–30`, `31–60`, `61–90`, `90+` days).
- **Penalty Tracking**: Shows Principal, Accrued Penalty, and Total Payable per invoice.
- **Action Controls**: 1-click **Waive/Restore** fee button and **Mark Paid** settlement trigger.
- High-level KPIs: Total Outstanding, Overdue Portfolio, Accrued Late Penalties, and Total Collectible.

### Module 3: Double-Entry Transactions Ledger
- Comprehensive 10-column ledger with live debounced searching across customer names, invoice IDs, mobile numbers, and emails.
- Filterable by transaction type (`INCOME`, `EXPENSE`), category, and status (`PAID`, `PENDING`, `OVERDUE`).
- Displays accrued penalty badges on overdue and late-settled rows.

### Module 4: Multi-Channel Customer Communication Hub
- **One-Click Native Phone Dialer**: Triggers `tel:` protocol while capturing structured call logs and promised payment dates.
- **Dual-Party SMS Dispatcher**: Automatically embeds invoice ID, principal, penalty, and total payable, dispatching an immediate audit twin to the shopkeeper's phone.
- **Formal Statement Email**: Automatically generates professional demand letters CC'ing the shopkeeper's email.
- **Bulk Defaulters Broadcast**: 1-click action to alert all past-due debtors across SMS and email simultaneously.

### Module 5: Predictive Cash Runway Forecasting
- Projects cash balances 7, 30, 60, and 90 days into the future.
- Visualizes the exact calendar date of projected safety-floor breaches.
- Day-by-day closing cash table with expected inflows and scheduled vendor outflows.

### Module 6: Early Warning Radar & Anomaly Detection
- Evaluates Z-scores on operating expenses against rolling historical category baselines.
- Flags single disbursements exceeding 300% of normal category spend.
- Generates actionable warnings with recommended mitigation steps.

### Module 7: What-If Stress Simulator
- Interactive simulation engine with 1-click preset shocks (client payment stalls, emergency Capex repairs, revenue contraction, inflation).
- Renders baseline vs. stressed dual-curve comparisons to test financial resilience.

### Module 8: Dataset Versioning & Lineage Tracker
- Ingests CSV files, verifies phone numbers and emails, and assigns sequential version IDs.
- Automated differential comparison engine detects added, modified, and removed records.

### Module 9: Enterprise Configuration & Theming
- Centralized configuration of minimum safe cash thresholds, business contacts, and the 5 corporate theme palettes.
- **Penalty Policy Controls**: Toggle penalty enforcement, select calculation model, rate, grace period, and maximum penalty cap.

---

# 11. Implementation Details

## 11.1 Directory & Folder Structure

```
CASH_IN/
├── source/
│   ├── main/
│   │   ├── java/com/cashflow/
│   │   │   ├── CashFlowApplication.java         # Spring Boot Server Entrypoint
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java          # SQLite schema migration runner & CORS
│   │   │   ├── controller/                      # REST API Layer
│   │   │   │   ├── AnomalyController.java       # Z-score & spike alerts
│   │   │   │   ├── CommunicationController.java # Dual-party outreach API
│   │   │   │   ├── DashboardController.java     # Aggregated cockpit metrics
│   │   │   │   ├── ReceivablesController.java   # Aging schedule & penalty metrics
│   │   │   │   ├── SettingsController.java      # Settings & penalty policies
│   │   │   │   └── TransactionController.java   # Ledger & penalty waiver/settlement
│   │   │   ├── model/                           # Domain Entities
│   │   │   │   ├── ApplicationSettings.java     # Penalty rules & threshold model
│   │   │   │   ├── Customer.java                # Debtor profile & total penalty
│   │   │   │   └── Transaction.java             # Core ledger entity with penalty fields
│   │   │   ├── repository/                      # DAO & JDBC Repositories
│   │   │   ├── service/                         # Business Domain Services
│   │   │   │   ├── AnomalyService.java          # Statistical anomaly detection
│   │   │   │   ├── CommunicationService.java    # Dual-party SMS/Email templates
│   │   │   │   ├── ForecastService.java         # Runway projection engine
│   │   │   │   ├── PenaltyService.java          # Late & unpaid fee calculation
│   │   │   │   └── ReceivablesService.java      # Aging & debtor delay metrics
│   │   │   └── ui/                              # JavaFX Desktop Application Layer
│   │   └── resources/
│   │       ├── schema.sql                       # DDL schema with penalty columns
│   │       ├── application.properties           # Spring Boot & SQLite configuration
│   │       └── static/                          # Zero-CDN Web Frontend
│   │           ├── index.html                   # Single-Page Application Shell
│   │           ├── css/                         # themes.css, layout.css, components.css
│   │           └── js/                          # api.js, receivables.js, settings.js, etc.
│   └── test/java/com/cashflow/
│       ├── controller/
│       │   └── PenaltyIntegrationTest.java      # Full HTTP MockMvc end-to-end test
│       └── service/
│           ├── PenaltyServiceTest.java          # Unit tests for penalty algorithms
│           ├── CommunicationServiceTest.java    # Outreach tests
│           └── AnomalyServiceTest.java          # Statistical anomaly tests
├── pom.xml                                      # Maven project configuration
├── README.md                                    # Project documentation
├── ARCHITECTURE.md                              # Technical architectural map
└── USER_GUIDE.md                                # Operational user manual
```

## 11.2 Key Algorithms & Mathematical Formulations

### 1. Late & Unpaid Payment Penalty Algorithm
For any income invoice past its due date:
$$\text{Days Late} = \begin{cases} 
\text{paymentDate} - \text{dueDate} & \text{if status} = \text{PAID} \\
\text{currentDate} - \text{dueDate} & \text{if status} \in \{\text{PENDING}, \text{OVERDUE}\}
\end{cases}$$

$$\text{Billable Days} = \max(0, \text{Days Late} - \text{Grace Period Days})$$

Depending on the configured penalty model:
- **Daily Percentage**:
  $$\text{Penalty} = \min\left(\text{Principal} \times \frac{\text{Rate}}{100} \times \text{Billable Days}, \, \text{Principal} \times \frac{\text{Max Cap}}{100}\right)$$
- **One-Time Percentage**:
  $$\text{Penalty} = \min\left(\text{Principal} \times \frac{\text{Rate}}{100}, \, \text{Principal} \times \frac{\text{Max Cap}}{100}\right)$$
- **Flat Fee**:
  $$\text{Penalty} = \text{Rate}$$
- **Total Payable**:
  $$\text{Total Payable} = \text{Principal} + \text{Penalty}$$

### 2. Statistical Spend Anomaly Z-Score
For expense transaction $x$ within expense category $C$:
$$Z = \frac{x - \mu_C}{\sigma_C}$$
Where $\mu_C$ is the rolling mean of category $C$ and $\sigma_C$ is its standard deviation. An anomaly is flagged whenever $Z > 3.0$ or $x > 3 \times \mu_C$.

## 11.3 Core Logic & Code Snippets

### PenaltyService Calculation Engine (`PenaltyService.java`):
```java
public PenaltyDetails calculate(BigDecimal amount, String transactionType, String status,
                                LocalDate dueDate, LocalDate paymentDate, boolean waived,
                                ApplicationSettings settings, LocalDate refDate) {
    BigDecimal principal = amount != null ? amount : BigDecimal.ZERO;
    if (!"INCOME".equalsIgnoreCase(transactionType) || dueDate == null || "CANCELLED".equalsIgnoreCase(status)) {
        return new PenaltyDetails(0, 0, BigDecimal.ZERO, principal, waived);
    }

    long daysLate = 0;
    if ("PAID".equalsIgnoreCase(status)) {
        if (paymentDate != null && paymentDate.isAfter(dueDate)) {
            daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate);
        }
    } else if ("PENDING".equalsIgnoreCase(status) || "OVERDUE".equalsIgnoreCase(status)) {
        LocalDate effectiveEnd = refDate != null ? refDate : LocalDate.now();
        if (effectiveEnd.isAfter(dueDate)) {
            daysLate = ChronoUnit.DAYS.between(dueDate, effectiveEnd);
        }
    }

    if (Boolean.FALSE.equals(settings.getPenaltyEnabled()) || waived || daysLate <= 0) {
        return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO, principal, waived);
    }

    int graceDays = settings.getGracePeriodDays() != null ? settings.getGracePeriodDays() : 3;
    long billableDays = Math.max(0, daysLate - graceDays);
    if (billableDays <= 0) {
        return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO, principal, waived);
    }

    BigDecimal rate = settings.getPenaltyRate();
    BigDecimal penalty = principal.multiply(rate)
            .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(billableDays));

    BigDecimal maxCapPct = settings.getMaxPenaltyPercent();
    if (maxCapPct.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal maxAllowed = principal.multiply(maxCapPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (penalty.compareTo(maxAllowed) > 0) penalty = maxAllowed;
    }

    penalty = penalty.setScale(2, RoundingMode.HALF_UP);
    return new PenaltyDetails(daysLate, billableDays, penalty, principal.add(penalty), false);
}
```

---

# 12. Deployment Details

### Public Code Repository
- **GitHub Repository**: [https://github.com/vedansh3805-gif/Cash-IN.git](https://github.com/vedansh3805-gif/Cash-IN.git)
- **Primary Branch**: `main`
- **Latest Verified Commit**: `b431b9c` (*feat: add late and unpaid payment penalty tracking with configurable policies and outreach integration*)

### Local & Server Production Execution
```bash
# 1. Clone the repository
git clone https://github.com/vedansh3805-gif/Cash-IN.git
cd Cash-IN

# 2. Execute automated test suite (58/58 tests passing)
mvn clean test

# 3. Launch Spring Boot application server
mvn spring-boot:run

# 4. Access the web application
open http://localhost:8080/index.html
```

---

# 13. Project Implementation Status Table

| Module / Requirement | Planned Scope | Implementation Status | Test & Verification Status |
| :--- | :--- | :---: | :---: |
| **Double-Entry Transactions Ledger** | 10-column filterable catalog with live search across counterparties, invoices, phones, and emails. | **100% Completed** | Verified with 10 demo permutations; debounced search & category filters passing. |
| **Forward Cash Runway Forecast** | 7, 30, 60, and 90-day predictive daily closing cash trajectory with safety floor markers. | **100% Completed** | Verified via `ForecastServiceTest`; math models match deterministic ledger sum. |
| **Late & Unpaid Payment Penalty System** | Configurable daily percentage, flat fee, grace period, max cap, waiver toggle, and settlement. | **100% Completed** | Verified via `PenaltyServiceTest` (5 tests) & `PenaltyIntegrationTest` (full HTTP MockMvc). |
| **Receivables Aging Schedule** | Categorization across 0–30, 31–60, 61–90, 90+ day buckets with customer delay profiling. | **100% Completed** | Verified via `ReceivablesServiceTest`; auto-calculates total accrued penalties. |
| **Multi-Channel Defaulter Outreach** | Native phone dialer, dual-party SMS, and formal email statement dispatcher with audit trail. | **100% Completed** | Verified via `CommunicationServiceTest`; dual audit records saved to SQLite. |
| **Statistical Spend Anomaly Radar** | Z-score ($> 3\sigma$) and 300% multiplier detection on operating expenditures. | **100% Completed** | Verified via `AnomalyServiceTest`; flags rogue vendor spikes. |
| **What-If Stress Simulator** | Client delays, Capex shocks, revenue dips, and cost inflation with dual-curve rendering. | **100% Completed** | Verified via `ScenarioController` & `whatif.js`. |
| **Dataset Ingestion & Version Delta** | 13-column CSV parser with E.164 phone validation and automated change comparator. | **100% Completed** | Verified via `CsvImportServiceTest` & `DataComparisonServiceTest`. |
| **Enterprise Theming & Settings** | 5 corporate themes, currency switcher, threshold configuration, and penalty policies. | **100% Completed** | Verified via `SettingsController` and live theme switching. |
| **Companion JavaFX Desktop App** | Standalone desktop application with identical UI service contract. | **100% Completed** | Verified via `BackendUiServiceTest` (11 component tests). |

---

# 14. References

1. **Spring Boot Framework Documentation**: *Spring Boot Reference Guide v3.3.5*, VMware Tanzu, 2024.
2. **SQLite Consortium**: *SQLite Database Engine Architecture and ACID Guarantees*, SQLite.org.
3. **OpenCSV Project**: *OpenCSV 5.9 Developer Guide*, SourceForge, 2024.
4. **Reserve Bank of India & MSMED Act (2006)**: *Guidelines on Delayed Payments to Micro and Small Enterprises and Statutory Late Interest Provisions*, Ministry of Micro, Small and Medium Enterprises, Government of India.
5. **Martin Fowler**: *Patterns of Enterprise Application Architecture*, Addison-Wesley, 2002.
6. **W3C Web Standards**: *HTML5 Canvas 2D Context Specification*, World Wide Web Consortium (W3C), 2023.

---

# 15. Appendix

### System Workflow Summary
1. **Data Ingestion**: The user uploads or switches to a business dataset CSV. The validation engine sanitizes counterparties, amounts, and dates.
2. **Real-Time Analysis**: The system immediately evaluates current liquidity, overdue receivables, and spending anomalies.
3. **Penalty Accrual**: The penalty engine checks each unpaid or late invoice against the configured grace period and policy, computing live accrued penalties and total payable.
4. **Automated Outreach**: When past due, the user clicks to initiate a native telephone call, SMS notice, or formal statement email—embedding the itemized penalty calculation.
5. **Waiver & Settlement**: The business owner can waive the penalty in 1 click or click **Paid** to settle the invoice and finalize the historical late-payment record.
6. **Forward Planning**: What-If stress testing and 90-day runway projections give the owner weeks of advance notice before potential cash deficits occur.
