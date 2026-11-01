# CashFlow Early Warning System — Architecture Guide

> **Baseline Architecture**: Pure Java Desktop Financial Analytics Application (Java 21 + JavaFX + Spring Boot + SQLite + Maven).

---

## 1. High-Level System Architecture

The application adopts the **MVC + Service Layer + DAO/Repository** desktop architecture specified in `project_draft.md`:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        JavaFX Presentation Tier                        │
│   (Sidebar Navigation, Dashboard, Forecast Charts, Tables, Themes)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                         CashFlowUiService Interface
                        (Decoupled Service Boundary)
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
┌───────────────────────────────┐           ┌────────────────────────────────┐
│   Complete Project Backend    │           │    Independent Frontend Mock   │
│   (BackendUiService.java)     │           │       (MockUiService.java)     │
│  - Spring Boot 3 Core         │           │  - 100% In-Memory Static Mock  │
│  - Forecasting & Warning Svc  │           │  - Zero Database               │
│  - CSV Import & Diff Engine   │           │  - Zero Spring Boot / Network  │
│  - SQLite Database (JDBC)     │           │  - Action Buttons Disconnected │
└───────────────────────────────┘           └────────────────────────────────┘
```

---

## 2. Directory Layout

```
/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/
├── pom.xml                                  <-- Maven Root Project descriptor
├── ARCHITECTURE.md                          <-- Technical architecture specification
├── FRONTEND_GUIDE.md                        <-- Frontend extraction & decoupling guide
├── USER_GUIDE.md                            <-- Step-by-step user manual
├── DECISIONS.md                             <-- Architectural decision record (ADR)
├── TEST_REPORT.md                           <-- Full QA verification test report
├── project_draft.md                         <-- Project specifications
│
├── source/                                  <-- COMPLETE WORKING PROJECT
│   ├── main/
│   │   ├── java/com/cashflow/
│   │   │   ├── CashFlowApplication.java     <-- Spring Boot entry point
│   │   │   ├── model/                       <-- Domain POJOs & DTOs:
│   │   │   │   ├── Transaction.java         <-- Features customerPhone, customerEmail
│   │   │   │   ├── Customer.java            <-- Features phone, email, delay history
│   │   │   │   ├── Business.java            <-- Features shopkeeper phone, email
│   │   │   │   ├── ApplicationSettings.java <-- Features shopkeeperPhone, shopkeeperEmail
│   │   │   │   ├── CommunicationLog.java    <-- Call, SMS, Email outreach audit model
│   │   │   │   ├── ForecastEntry.java
│   │   │   │   ├── Warning.java
│   │   │   │   ├── Anomaly.java
│   │   │   │   └── RecurringPayment.java
│   │   │   ├── repository/                  <-- SQLite JDBC Repositories:
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── SettingsRepository.java
│   │   │   │   ├── CommunicationLogRepository.java
│   │   │   │   ├── RecurringPaymentRepository.java
│   │   │   │   ├── ForecastRepository.java
│   │   │   │   ├── WarningRepository.java
│   │   │   │   ├── AnomalyRepository.java
│   │   │   │   └── DatasetVersionRepository.java
│   │   │   ├── service/                     <-- Core math, outreach & analytics engines:
│   │   │   │   ├── CsvImportService.java     <-- Ingestion & contact extraction
│   │   │   │   ├── CsvValidationService.java <-- Regex phone & email format validator
│   │   │   │   ├── ReceivablesService.java   <-- Debt metrics & delay calculator
│   │   │   │   ├── CommunicationService.java <-- Multi-channel dialer, SMS & Email engine
│   │   │   │   ├── DataComparisonService.java<-- Version delta diff engine
│   │   │   │   ├── ForecastService.java      <-- Day-by-day cash trajectory
│   │   │   │   ├── WarningService.java       <-- Liquidity breach & overdue warnings
│   │   │   │   └── AnomalyService.java       <-- 3x spending outlier detector
│   │   │   ├── controller/                  <-- Optional REST API controllers:
│   │   │   │   ├── CommunicationController.java <-- /api/communication/call, sms, email, logs
│   │   │   │   ├── ReceivablesController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   ├── ForecastController.java
│   │   │   │   ├── WarningController.java
│   │   │   │   ├── CsvController.java
│   │   │   │   └── SettingsController.java
│   │   │   └── ui/                          <-- JavaFX Desktop Presentation Tier:
│   │   │       ├── CashFlowDesktopApp.java  <-- Main desktop GUI entry point
│   │   │       ├── service/                 <-- UI Services (CashFlowUiService, BackendUiService)
│   │   │       ├── theme/ThemeManager.java  <-- JavaFX CSS Theme loader
│   │   │       └── view/                    <-- JavaFX Screens:
│   │   │           ├── MainLayout.java      <-- Sidebar & screen navigation
│   │   │           ├── DashboardView.java   <-- KPI Cards & Cash Charts
│   │   │           ├── TransactionsView.java<-- TableView with buyer contacts & search
│   │   │           ├── ForecastView.java    <-- LineChart trajectory
│   │   │           ├── WarningsView.java    <-- Alerts with Defaulter Outreach buttons
│   │   │           ├── ReceivablesView.java <-- Aging, Phone/Email cols & Outreach Modals
│   │   │           ├── CommunicationView.java<-- Dedicated Customer Contact & Outreach Page
│   │   │           ├── ExpensesView.java    <-- Categorized spending & recurring
│   │   │           ├── VersionsView.java    <-- Version lineage & diffs
│   │   │           ├── ScenariosView.java   <-- What-If simulation
│   │   │           ├── UploadView.java      <-- CSV file chooser with contact guidance
│   │   │           ├── ReportsView.java     <-- CSV ledger export
│   │   │           └── SettingsView.java    <-- Themes & Shopkeeper Phone/Email setup
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql                   <-- SQLite DDL with communication tables
│   │       ├── sample-data.csv              <-- 13-column test dataset
│   │       ├── sample-data-v2.csv           <-- 13-column delta dataset
│   │       ├── styles/                      <-- 5 JavaFX CSS Themes
│   │       └── static/                      <-- FULLY READY BROWSER WEB APPLICATION
│   │           ├── index.html               <-- Responsive SPA with sidebar & 11 pages
│   │           ├── css/                     <-- layout.css, components.css, pages.css, themes.css
│   │           └── js/                      <-- app.js, api.js, communication.js, receivables.js, etc.
│   └── test/java/com/cashflow/              <-- JUnit 5 test suites (47 tests)
│
└── frontend-only/                           <-- INDEPENDENT FRONTEND (MOCK)
    ├── pom.xml                              <-- Pure JavaFX dependencies (NO Spring, NO SQLite)
    └── source/main/
        ├── java/com/cashflow/
        │   ├── model/                       <-- DTOs & Domain POJOs (features contacts)
        │   └── ui/
        │       ├── StandaloneFrontendApp.java <-- Launchable Desktop Window
        │       ├── service/MockUiService.java <-- In-memory mock service with outreach mocks
        │       ├── theme/ThemeManager.java
        │       └── view/                    <-- Exact same UI views & CommunicationView
        └── resources/styles/                <-- Exact same 5 JavaFX CSS themes
```

---

## 3. Core Component Responsibilities

### Presentation Layer (`com.cashflow.ui.view` & Web App `static/`)
- **`MainLayout` / `index.html`**: Maintains the sidebar navigation and view swapping across both Desktop (JavaFX) and Web (HTML5/CSS3/Vanilla JS) with zero emojis, modern SVG vector icons, breadcrumb paths, and live business health indicator.
- **`EnterpriseChart` (`charts.js`)**: **Native Zero-Dependency HTML5 Canvas Chart Engine**:
  - Provides a 100% offline, HiDPI Retina-scaled charting engine eliminating all external CDN dependencies.
  - Supports Line charts (multi-series, area gradients, dashed threshold lines), Bar charts (single and grouped comparative), Horizontal Bar charts, and Doughnut/Pie charts.
  - Interactive tooltip system dynamically calculates mouse hover coordinates, rendering high-contrast floating badges with formatted currency and percentage figures.
- **`DashboardView` / `dashboard.js`**: Renders 8 financial KPI cards, Cash Dynamics comparative bar chart, Receivables Risk Exposure doughnut chart, and daily liquidity trendline.
- **`TransactionsView` / `transactions.js`**: Provides real-time filtering, search by keyword, customer name, mobile number, and email address, multi-column sorting, and detail modal.
- **`ForecastView` / `forecast.js`**: Dynamic cash trajectory visualization over 7/30/60/90-day horizons with dashed minimum cash safety threshold, minimum cash floor, closing balance, and safety buffer metrics.
- **`WarningsView` / `warnings.js`**: Displays alerts for minimum cash breaches, overdue receivables, and statistical expense anomalies, with a direct "Contact Defaulter" outreach action.
- **`ReceivablesView` / `receivables.js`**: Tracks overdue invoices, displays buyer mobile numbers and email addresses, aging categories (`current`, `1-30 days`, `31-60 days`, `61-90 days`, `90+ days`), and interactive outreach modals.
- **`CommunicationView` / `communication.js`**: **Dedicated Customer Communication & Multi-Channel Outreach Page**:
  - Displays customer mobile number (`customer_phone`) and email address (`customer_email`) with click-to-dial `tel:` and click-to-email `mailto:` links.
  - Actionable buttons for every debtor:
    1. **Call Customer**: Opens Call modal with customer phone, invoice breakdown, shopkeeper call notes, and direct dial trigger.
    2. **Send SMS**: Dispatches personalized collection reminder to customer mobile with shopkeeper audit copy.
    3. **Send Email**: Delivers professional formal billing statement to customer email with shopkeeper CC copy.
    4. **Notify Both**: 1-click dual-party notification dispatching SMS and Email simultaneously to customer mobile/email and shopkeeper audit.
  - **Bulk Alert All Defaulters**: Batches dual-party notifications to all delinquent accounts.
  - **Permanent Communication Audit Trail**: Real-time filterable log of all outbound calls, SMS reminders, and billing emails with timestamps and delivery status.
- **`ExpensesView` / `expenses.js`**: Displays categorical expense breakdowns and multi-month forward expense projections derived from active recurring payments.
- **`VersionsView` / `versions.js`**: Interactive version history inspector and true differential comparison engine between arbitrary dataset versions.
- **`SettingsView` / `settings.js`**: Allows shopkeepers to configure shopkeeper mobile number and email address alongside safety thresholds and theme selection.
- **`UploadView` / `app.js`**: Drag-and-drop CSV uploader parsing 13-column financial files with automatic contact extraction and validation.

### Service Layer (`com.cashflow.service`)
- **`CommunicationService`**: Orchestrates multi-channel outreach (`initiateCall`, `sendSmsNotice`, `sendEmailNotice`, `notifyBothParties`), interacts with `CommunicationLogRepository`, and provides audit trail access.
- **`ReceivablesService`**: Aggregates customer payment metrics, average delay days, total overdue exposure, and links buyer contact information.
- **`CsvValidationService`**: Validates CSV structural integrity, detects dynamic contact headers (`customer_phone`, `buyer_mobile`, `customer_email`, `buyer_email`), and validates phone/email formats using regex.
- **`CsvImportService`**: Ingests validated rows, creates dataset versions, maps contact columns, and triggers receivables and forecasting updates.
- **`ForecastService`**: Calculates day-by-day cash trajectory:
  $$\text{Closing Cash} = \text{Opening Cash} + \text{Inflow} - \text{Outflow}$$
  Anchors start dates dynamically to dataset reference periods, accurately scheduling recurring payments (`DAILY`, `WEEKLY`, `MONTHLY`) and overdue receivables recovery.
- **`WarningService`**: Evaluates closing cash against `minimum_cash_threshold`, scanning for impending cash shortfalls and overdue invoices.
- **`AnomalyService`**: Detects unusually large expenses ($>3\times$ average) and structural deviations.
- **`DataComparisonService` & `VersionController`**: True differential comparison engine executing bidirectional map diffs between any two versions $v_1$ and $v_2$, isolating `ADDED`, `MODIFIED`, `REMOVED`, and `UNCHANGED` records with granular field-level changes.

### Data Layer (`com.cashflow.repository` & `schema.sql`)
- Uses Spring JDBC `JdbcTemplate` with parameterized queries on embedded SQLite (`cashflow.db`).
- **`DatabaseConfig.java`**: Implements an automated, non-destructive migration runner that safely alters existing tables (`transaction_record`, `customer`, `business`, `application_settings`) and initializes `communication_log`.
- **`CommunicationLogRepository`**: Persists and queries all outreach events with timestamps, channels, recipients, and delivery status.

