# CashFlow Frontend — Complete Architectural & Technical Master Guide

> **Document Purpose**: This file (`Explain.md`) is an exhaustive, self-contained technical blueprint of the `frontend-only` independent desktop application. You can upload this document directly into ChatGPT (or any AI assistant) to analyze, explain, test, or expand any aspect of the frontend architecture one by one.

---

## 🤖 ChatGPT Prompting Instructions (How to Use This File)

If you have uploaded this file into ChatGPT, copy and paste any of the following prompts into your chat session to explore specific modules:

* **To understand the decoupled architecture**:
  > *"Based on Section 2 and Section 6 of Explain.md, explain how the JavaFX frontend stays 100% decoupled from any backend database using the `CashFlowUiService` interface."*
* **To explore a specific screen**:
  > *"Act as a Senior JavaFX Engineer. Walk me step-by-step through `DashboardView.java` (Section 8.1) and explain how the KPI cards, BarChart, and PieChart are constructed and styled."*
* **To learn the CSS theming system**:
  > *"Explain how the dynamic theme switcher in Section 9 works. How does `ThemeManager.java` swap CSS stylesheets at runtime without reloading the JavaFX stage?"*
* **To integrate with a real backend**:
  > *"Write a Java 21 implementation of `CashFlowUiService` using Java's built-in `HttpClient` to call a Spring Boot REST API instead of `MockUiService`."*

---

## Table of Contents

1. [Executive Summary & Purpose](#1-executive-summary--purpose)
2. [Architectural Philosophy: Decoupled UI & Zero-Backend](#2-architectural-philosophy-decoupled-ui--zero-backend)
3. [Technology Stack & Build System (`pom.xml`)](#3-technology-stack--build-system-pomxml)
4. [Project Directory & File Inventory](#4-project-directory--file-inventory)
5. [Data Models & Data Transfer Objects (DTOs)](#5-data-models--data-transfer-objects-dtos)
6. [The Service Abstraction Layer](#6-the-service-abstraction-layer)
   - 6.1 [`CashFlowUiService.java` (The Contract)](#61-cashflowuiservicejava-the-contract)
   - 6.2 [`MockUiService.java` (The In-Memory Provider)](#62-mockuiservicejava-the-in-memory-provider)
   - 6.3 Financial Simulation & Trajectory Math
7. [Application Lifecycle & Main Navigation Shell](#7-application-lifecycle--main-navigation-shell)
   - 7.1 [`StandaloneFrontendApp.java` (Entry Point)](#71-standalonefrontendappjava-entry-point)
   - 7.2 [`MainLayout.java` (Sidebar & Navigation Hub)](#72-mainlayoutjava-sidebar--navigation-hub)
8. [Comprehensive Screen-by-Screen Breakdown](#8-comprehensive-screen-by-screen-breakdown)
   - 8.1 `DashboardView.java` (Financial Health & KPI Cards)
   - 8.2 `TransactionsView.java` (Filterable Ledger & Search)
   - 8.3 `ForecastView.java` (Predictive Trajectory LineChart)
   - 8.4 `WarningsView.java` (Early Warning Alerts & Anomaly Radar)
   - 8.5 `ReceivablesView.java` (Aging Buckets & Invoices)
   - 8.6 `ExpensesView.java` (Category Spending & Recurring Costs)
   - 8.7 `ScenariosView.java` (What-If Simulation Engine)
   - 8.8 `VersionsView.java` (Audit Trail & Change Lineage)
   - 8.9 `UploadView.java` (CSV Ingestion & Validation Log)
   - 8.10 `ReportsView.java` (CSV Export & Statement Generator)
   - 8.11 `SettingsView.java` (Themes & Threshold Parameters)
   - 8.12 `CommunicationView.java` (Customer Communication Center & Outreach)
9. [The CSS Theming Engine & Visual Design](#9-the-css-theming-engine--visual-design)
   - 9.1 How `ThemeManager.java` Swaps Stylesheets at Runtime
   - 9.2 The 5 Built-in Visual Palettes
   - 9.3 Semantic Colors & CSS Variables
10. [How to Build, Run & Package](#10-how-to-build-run--package)
11. [Backend Integration Guide (Connecting Real APIs)](#11-backend-integration-guide-connecting-real-apis)

---

## 1. Executive Summary & Purpose

The `frontend-only` subproject is an **independent, zero-backend, zero-database** pure Java desktop application built using **JavaFX 21** and **Maven**.

### What problem does it solve?
In enterprise financial software development, UI development often gets bottlenecked by backend database schemas, running servers, authentication endpoints, or network latency.

`frontend-only` provides:
* **Immediate Visual Feedback**: Designers, QA engineers, and stakeholders can launch the full desktop application in less than 2 seconds without starting Spring Boot, Docker, SQLite, or network services.
* **100% Feature-Complete Interface**: Every screen—from multi-horizon line charts, financial KPI grids, and filterable transaction tables to what-if shock simulators—is fully functional and interactive.
* **Pluggable Contract**: Built against an interface (`CashFlowUiService`). When the backend is ready, developers can connect it simply by implementing this single interface without altering a single UI view.

---

## 2. Architectural Philosophy: Decoupled UI & Zero-Backend

The application is engineered around the **Clean Architecture / Inversion of Control** pattern:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           JavaFX Presentation Tier                           │
│  (MainLayout, DashboardView, ForecastView, TransactionsView, ThemeManager)  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Calls interface methods
                                       ▼
                       ┌───────────────────────────────┐
                       │      CashFlowUiService        │
                       │     (Java Interface)          │
                       └───────────────┬───────────────┘
                                       │ Implemented by
                                       ▼
                       ┌───────────────────────────────┐
                       │        MockUiService          │
                       │  - In-memory data collections │
                       │  - Synthetic mathematical curves│
                       │  - Zero JDBC / Zero Network   │
                       └───────────────────────────────┘
```

### Key Decoupling Principles:
1. **Views Never Know Data Sources**: None of the JavaFX views (`DashboardView`, `TransactionsView`, etc.) import SQL, SQLite, JDBC, Spring Boot, or HTTP classes. They only know about `CashFlowUiService`.
2. **Deterministic Mock Data**: `MockUiService` synthesizes a realistic 90-day cash curve featuring payroll dips, bi-weekly invoice settlements, and weekend spending patterns, ensuring charts never render empty.
3. **Safe Sandbox Execution**: Action buttons (such as "Save Settings" or "Add Recurring Payment") operate in memory or log notices to the console, guaranteeing that the application produces zero destructive side effects on the user's filesystem.

---

## 3. Technology Stack & Build System (`pom.xml`)

The `frontend-only` build system uses Apache Maven and requires **Java 21**.

### Dependencies in `pom.xml`:
* **`org.openjfx:javafx-controls:21.0.2`**: Core UI widgets (`Button`, `TableView`, `ComboBox`, `Label`, `TextField`, `ScrollPane`).
* **`org.openjfx:javafx-fxml:21.0.2`**: JavaFX markup and layout engine support.
* **`org.openjfx:javafx-swing:21.0.2`**: Swing/AWT interop utilities.
* **`org.projectlombok:lombok:1.18.34`** (Scope: `provided`): Eliminates boilerplate getters, setters, and constructors in DTO models.
* **`org.openjfx:javafx-maven-plugin:0.0.8`**: Compiles and executes the JavaFX runtime with the modular path configured automatically.

### Root `pom.xml` Specification:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.cashflow</groupId>
    <artifactId>cashflow-frontend-only</artifactId>
    <version>1.0.0</version>
    <name>cashflow-frontend-only</name>
    <description>Pure Java Desktop Independent Frontend (No Backend, No DB, No Spring Boot)</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-swing</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>source/main/java</sourceDirectory>
        <resources>
            <resource>
                <directory>source/main/resources</directory>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>com.cashflow.ui.StandaloneFrontendApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 4. Project Directory & File Inventory

```text
frontend-only/
├── pom.xml                                      # Maven build descriptor
├── documentation/
│   ├── Front_ARCHITECTURE_AND_EXTRACTION_GUIDE.md # Architectural decoupling notes
│   └── Front_USER_GUIDE.md                      # End-user quickstart manual
└── source/main/
    ├── java/com/cashflow/
    │   ├── model/                               # Data Models / DTOs
    │   │   ├── Transaction.java                 # Financial ledger record
    │   │   ├── ForecastEntry.java               # Single projected day cash snapshot
    │   │   ├── Warning.java                     # Explainable alert / warning object
    │   │   └── dto/
    │   │       ├── DashboardMetrics.java        # KPI and overview aggregated metrics
    │   │       └── CsvImportResult.java         # CSV parsing & change-detection summary
    │   └── ui/
    │       ├── StandaloneFrontendApp.java       # JavaFX desktop application launcher
    │       ├── service/
    │       │   ├── CashFlowUiService.java       # Boundary interface for UI views
    │       │   └── MockUiService.java           # In-memory mock data & simulation engine
    │       ├── theme/
    │       │   └── ThemeManager.java            # Runtime CSS stylesheet manager
    │       └── view/                            # 11 Modular JavaFX Desktop Screens
    │           ├── MainLayout.java              # Root BorderPane with sidebar & content area
    │           ├── DashboardView.java           # 8 KPI cards, BarChart, PieChart
    │           ├── TransactionsView.java        # Filterable, searchable ledger TableView
    │           ├── ForecastView.java            # Trajectory LineChart (7/30/60/90 days)
    │           ├── WarningsView.java            # Early warning alerts & anomalies
    │           ├── ReceivablesView.java         # Aging analysis & unpaid customer invoices
    │           ├── ExpensesView.java            # Category spending breakdown & recurring costs
    │           ├── ScenariosView.java           # What-If hypothetical shock simulator
    │           ├── VersionsView.java            # Dataset version lineage & transaction diffs
    │           ├── UploadView.java              # CSV file chooser & simulated ingestion log
    │           ├── ReportsView.java             # Statement generation & CSV export
    │           └── SettingsView.java            # Themes, risk thresholds, preferences
    └── resources/
        └── styles/                              # 5 JavaFX CSS Theme Stylesheets
            ├── theme-arctic-ledger.css          # Dark slate / cyan (Default)
            ├── theme-forest-ledger.css          # Deep emerald / gold-mint
            ├── theme-copper-desk.css            # Warm dark bronze / amber
            ├── theme-ocean-glass.css            # Cobalt navy / turquoise
            └── theme-paper-ink.css              # Editorial light mode (ivory / charcoal)
```

---

## 5. Data Models & Data Transfer Objects (DTOs)

The models in `com.cashflow.model` are pure POJOs annotated with Project Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`).

### 5.1 `Transaction.java`
Represents an individual cash inflow or outflow in the ledger.
```java
public class Transaction {
    private Long id;
    private String transactionId;   // e.g. "TXN-MOCK-001"
    private String versionId;       // Lineage reference
    private LocalDate transactionDate;
    private String transactionType; // "INCOME" or "EXPENSE"
    private String category;        // e.g. "Client Retainer", "Office Lease"
    private String description;     // Human-readable detail
    private BigDecimal amount;      // Precise financial currency amount
    private String customer;        // Present if INCOME
    private String customerPhone;   // Buyer mobile number for calls & SMS
    private String customerEmail;   // Buyer email address for statement notices
    private String supplier;        // Present if EXPENSE
    private LocalDate dueDate;      // Due date for receivables/payables
    private LocalDate paymentDate;  // Actual settlement date
    private String status;          // "PAID", "PENDING", "OVERDUE"
}
```

### 5.2 `CommunicationLog.java`
Represents an outreach audit record generated whenever a shopkeeper places a call, sends an SMS, or dispatches an email notice to a buyer or shopkeeper:
```java
public class CommunicationLog {
    private Long id;
    private String logId;
    private String transactionId;
    private String customerName;
    private String channel;          // "CALL", "SMS", "EMAIL", "BOTH"
    private String recipientType;    // "BUYER", "SHOPKEEPER", "BOTH"
    private String recipientContact; // Phone number or email address
    private String messageBody;      // Text sent or call transcript notes
    private LocalDateTime sentAt;    // Timestamp of transmission
    private String status;           // "CONNECTED", "DELIVERED", "SENT", "FAILED"
}
```

### 5.3 `ForecastEntry.java`
Represents a single projected calendar day in the cash flow simulation.
```java
public class ForecastEntry {
    private String forecastId;
    private String versionId;
    private LocalDate forecastDate;
    private BigDecimal openingCash;     // Cash balance starting the day
    private BigDecimal expectedInflow;  // Scheduled customer collections
    private BigDecimal expectedOutflow; // Scheduled bills & recurring expenses
    private BigDecimal closingCash;     // Ending cash (Opening + Inflow - Outflow)
}
```

### 5.3 `Warning.java`
Represents an actionable alert generated when the projected cash balance dips below defined business safety thresholds or when receivables are overdue.
```java
public class Warning {
    private String warningId;
    private String versionId;
    private String warningType;   // "LOW_PROJECTED_CASH", "OVERDUE_RECEIVABLE"
    private String severity;      // "CRITICAL", "WARNING", "INFO"
    private LocalDate warningDate;
    private LocalDate projectedDate; // When the shortfall is expected to occur
    private String message;       // Explainable root-cause message
    private String status;        // "ACTIVE", "RESOLVED"
}
```

### 5.4 `DashboardMetrics.java` (DTO)
Aggregated financial snapshot driving the main dashboard:
```java
public class DashboardMetrics {
    private BigDecimal currentCash;
    private BigDecimal expectedIncoming;
    private BigDecimal upcomingExpenses;
    private BigDecimal pendingReceivables;
    private BigDecimal overdueReceivables;
    private BigDecimal netCashFlow;
    private BigDecimal forecastedMinCash;
    private String riskStatus;           // "LOW", "MEDIUM", "HIGH"
    private int pendingReceivablesCount;
    private int overdueCount;
    private int warningCount;
    private int forecastDays;
    // ... Additional collections for summary charts
}
```

### 5.5 `CsvImportResult.java` (DTO)
Summary object returned after ingesting a transaction file:
```java
public class CsvImportResult {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int added;      // Delta engine: newly inserted records
    private int modified;   // Delta engine: records with changed amount/status
    private int removed;    // Delta engine: records missing from previous extract
    private int unchanged;  // Delta engine: identical records
    private int versionNumber;
    private List<String> errors;
    private String versionId;
}
```

---

## 6. The Service Abstraction Layer

### 6.1 `CashFlowUiService.java` (The Contract)
This interface completely isolates the presentation tier from the data layer:
```java
public interface CashFlowUiService {
    DashboardMetrics getDashboardMetrics();
    List<Transaction> getTransactions(String search, String type, String category, String status);
    List<ForecastEntry> getForecast(int days);
    List<Warning> getWarnings();
    List<Map<String, Object>> getAnomalies();
    Map<String, Object> getReceivablesAnalysis();
    Map<String, Object> getExpenseAnalysis();
    List<Map<String, Object>> getRecurringPayments();
    void addRecurringPayment(String description, String category, BigDecimal amount, String frequency, String nextDueDate);
    void deleteRecurringPayment(String id);
    List<Map<String, Object>> getVersions();
    Map<String, Object> compareVersions(String v1, String v2);
    List<Map<String, Object>> getTimeline();
    Map<String, Object> runScenarioSimulation(String name, String adjustmentType, BigDecimal amount, int delayDays);
    CsvImportResult importCsv(File file) throws Exception;
    Map<String, Object> getSettings();
    void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency, String shopkeeperPhone, String shopkeeperEmail);
    File exportReportCsv(File destination) throws Exception;

    // Multi-Channel Communication & Defaulter Outreach
    CommunicationLog initiateCall(String transactionId);
    CommunicationLog sendSms(String transactionId, String message);
    CommunicationLog sendEmail(String transactionId, String subject, String body);
    Map<String, Object> notifyBothParties(String transactionId);
    List<CommunicationLog> getCommunicationLogs(String transactionId);
}
```

### 6.2 `MockUiService.java` (The In-Memory Provider)
`MockUiService` implements `CashFlowUiService` without any database or network dependency.
* In its constructor (`initMockData()`), it constructs realistic transaction records with dates relative to `LocalDate.now()`.
* It provides a 90-day mathematical cash trajectory:
  ```java
  BigDecimal cash = new BigDecimal("105000.00");
  for (int i = 0; i < 90; i++) {
      LocalDate d = today.plusDays(i);
      BigDecimal in = BigDecimal.ZERO;
      BigDecimal out = BigDecimal.ZERO;

      if (i % 7 == 2)  in  = in.add(new BigDecimal("35000.00")); // Weekly collections
      if (i % 15 == 0) in  = in.add(new BigDecimal("75000.00")); // Fortnightly milestone
      if (i % 30 == 5) out = out.add(new BigDecimal("45000.00")); // Monthly rent
      if (i % 30 == 1) out = out.add(new BigDecimal("60000.00")); // Monthly payroll
      if (i % 10 == 4) out = out.add(new BigDecimal("12000.00")); // Utilities / SaaS

      cash = cash.add(in).subtract(out);
      mockForecast.add(new ForecastEntry(UUID.randomUUID().toString(), "V-MOCK", d, 
              cash.subtract(in).add(out), in, out, cash));
  }
  ```

### 6.3 What-If Shock Simulation Algorithm
In `runScenarioSimulation(...)`, `MockUiService` generates a comparative trajectory against the baseline:
* If the user simulates `DELAY_RECEIVABLES`, customer payments are postponed, causing the closing cash to temporarily dip by the shock amount during the delay window.
* If the user selects `INCREASE_EXPENSES` or `UNEXPECTED_EXPENSE`, future cash balances drop by the shock amount across all forward days.
* Returns both the `baseline` and `scenario` lists, allowing `ScenariosView` to plot both series on the same chart.

---

## 7. Application Lifecycle & Main Navigation Shell

### 7.1 `StandaloneFrontendApp.java` (Entry Point)
```java
public class StandaloneFrontendApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        CashFlowUiService mockService = new MockUiService();
        MainLayout root = new MainLayout(mockService);
        Scene scene = new Scene(root, 1200, 800);

        Map<String, Object> settings = mockService.getSettings();
        String theme = (String) settings.getOrDefault("theme", "arctic-ledger");
        ThemeManager.applyTheme(scene, theme);

        primaryStage.setTitle("CashFlow Independent Frontend [Pure Java Desktop - No Backend / No DB]");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }
}
```

### 7.2 `MainLayout.java` (Sidebar & Navigation Hub)
`MainLayout` extends JavaFX `BorderPane`:
* **Left (`BorderPane.setLeft`)**: Sidebar containing the brand logo (`CASH_IN: EARLY WARNING SYSTEM`) and grouped navigation buttons.
  * **OVERVIEW**: Dashboard
  * **ANALYZE**: Transactions, Forecast, Warnings & Anomalies, Receivables, Expenses
  * **INVESTIGATE**: Versions & Changes, What-If Simulator
  * **DATA & SYSTEM**: Upload CSV, Reports & Export, Settings
* **Center (`BorderPane.setCenter`)**: A `StackPane contentArea`. When a user clicks a sidebar button:
  1. `navigateTo(String key)` is called.
  2. The clicked button gains the `.active` CSS class; all other buttons lose it.
  3. The appropriate view (`DashboardView`, `ForecastView`, etc.) is instantiated and set into `contentArea.getChildren().setAll(view)`.

---

## 8. Comprehensive Screen-by-Screen Breakdown

### 8.1 `DashboardView.java`
* **Purpose**: Primary executive control center for real-time liquidity and risk monitoring.
* **Layout**: A `ScrollPane` wrapping a vertical container (`VBox`).
* **KPI Card Grid (4x2)**:
  1. *Current Cash*: Liquid in-bank balance (`₹1,05,000.00`).
  2. *Expected Inflow*: Pending and due collections (`₹1,40,000.00`).
  3. *Upcoming Expenses*: Invoices and recurring commitments (`₹45,000.00`).
  4. *Net Cash Flow*: Inflow minus outflow (`₹95,000.00`).
  5. *Pending Receivables*: Count and sum of pending customer payments.
  6. *Overdue Receivables*: Invoices past due date highlighted in alert red (`-color-danger`).
  7. *Forecasted Min Cash*: Lowest projected cash balance in next 90 days.
  8. *Risk Assessment*: Dynamic badge (`LOW`, `MEDIUM`, or `HIGH`).
* **Charts Section (`HBox`)**:
  * **Cash Dynamics BarChart**: Four color-coded bars:
    * Current Cash: `#0ea5e9` (Sky Blue)
    * Expected Incoming: `#10b981` (Emerald Green)
    * Upcoming Expenses: `#f43f5e` (Rose/Red)
    * Net Flow: `#8b5cf6` (Purple/Indigo)
  * **Receivables Aging PieChart**: Visualizes proportion of On-Time Pending vs Overdue invoices.

### 8.2 `TransactionsView.java`
* **Purpose**: Full searchable, filterable accounting ledger.
* **Controls**:
  * `TextField searchField`: Real-time text search across description, customer, supplier, and transaction ID.
  * `ComboBox<String> typeFilter`: Filters `ALL`, `INCOME`, `EXPENSE`.
  * `ComboBox<String> statusFilter`: Filters `ALL`, `PAID`, `PENDING`, `OVERDUE`.
  * `TableView<Transaction>`: Columns for ID, Date, Type, Category, Description, Entity, Amount (formatted in Indian Rupees `₹`), and Status.
  * Real-time record counter badge (`X record(s) loaded`).

### 8.3 `ForecastView.java`
* **Purpose**: Visualizes forward-looking cash runway.
* **Controls**:
  * `ComboBox<Integer> daysCombo`: Selectable projection horizons: `7`, `30`, `60`, and `90` days.
  * `LineChart<String, Number>`: Plots the projected cash floor curve over time.
  * Automatic data sampling: When viewing 60 or 90 days, data points are sampled to prevent horizontal axis clutter while preserving accurate peaks and troughs.
  * Metric summary badges: Display Forecast Horizon, Projected Minimum Cash, and Estimated Closing Cash.

### 8.4 `WarningsView.java`
* **Purpose**: Early warning alert center and statistical anomaly radar.
* **Components**:
  * **Active Financial Warnings Table**: Lists severity (`CRITICAL`, `WARNING`), warning type, projected breach date, and an explainable root-cause sentence.
  * **Actionable Defaulter Resolution**: When customer delay risks are flagged, a direct "Contact Defaulter" button allows the user to jump directly to outreach for that customer.
  * **Detected Financial Anomalies Table**: Highlights transactions flagged by statistical anomaly detection (e.g., expenses exceeding 2x or 3x historical standard deviations), listing Transaction ID, Anomaly Type, Actual Amount, Expected Value, and Analytical Explanation.

### 8.5 `ReceivablesView.java`
* **Purpose**: Tracks accounts receivable, delayed collections, aging exposure, and multi-channel customer outreach.
* **Components & Columns**:
  * Summary KPI cards: Total Receivables, Overdue Amount, and Outstanding Invoice Count.
  * `TableView<Transaction>`: Lists outstanding invoices with Invoice ID, Customer Name, **Buyer Phone**, **Buyer Email**, Due Date, Amount, and Status (`PENDING` or `OVERDUE`).
* **Multi-Channel Outreach Modals & Actions**:
  * 📞 **Call**: Launches `CallDialog` showing customer phone, invoice amount, overdue delay, and dialer status.
  * 💬 **SMS**: Launches `SmsDialog` with pre-filled SMS template and character counter.
  * ✉️ **Email**: Launches `EmailDialog` with pre-filled invoice billing notice and payment details.
  * 🔄 **Notify Both**: Simultaneous dual-party notification dispatching reminders to both the Buyer and the Shopkeeper.
  * 🚨 **Alert All Defaulters**: Single-click bulk outreach batching reminders to all overdue customer accounts.
  * 📜 **View Audit Trail**: Launches `CommunicationHistoryDialog` displaying complete chronological logs with timestamps, channels, recipients, and delivery confirmations.

### 8.6 `ExpensesView.java`
* **Purpose**: Categorized spending analysis and recurring obligations.
* **Components**:
  * `PieChart categoryChart`: Visualizes spending distribution (Office Lease, Payroll, Utilities, Consulting, etc.).
  * Total Realized Expenses KPI card.
  * `TableView` of Scheduled Recurring Payments: Lists Obligation Description, Category, Scheduled Amount, Frequency (`MONTHLY`, `WEEKLY`), and Next Due Date.

### 8.7 `ScenariosView.java`
* **Purpose**: What-If stress tester allowing finance teams to model hypothetical shocks without mutating real data.
* **Input Grid**:
  * Scenario Title (`TextField`)
  * Hypothetical Shock Dropdown (`DELAY_RECEIVABLES`, `INCREASE_EXPENSES`, `UNEXPECTED_EXPENSE`)
  * Shock Amount in ₹ (`TextField`)
  * Delay Impact in Days (`TextField`)
  * "Run What-If Simulation" Button
* **Comparative LineChart**:
  * Series 1: Baseline 30-day projection (blue).
  * Series 2: Simulated trajectory under stress (orange/red).
  * Immediately reveals when and by how much the company would breach its safe minimum cash threshold.

### 8.8 `VersionsView.java`
* **Purpose**: Complete audit lineage tracking changes between different CSV uploads.
* **Components**:
  * **Dataset Versions Table**: Lists Version Number (`V1`, `V2`), Source Filename, Upload Timestamp, and Total Records.
  * **Transaction Delta Table**: Highlights granular row-level differences:
    * Transaction ID
    * Change Type (`ADDED`, `MODIFIED`, `REMOVED`)
    * Field Name changed (e.g. `amount`, `status`)
    * Previous Value vs New Value

### 8.9 `UploadView.java`
* **Purpose**: Ingestion portal for periodic CSV accounting extracts.
* **Components**:
  * Interactive file browser using JavaFX `FileChooser` filtering `*.csv`.
  * Ingestion guidance explaining the 13-column schema with `customer_phone` and `customer_email`.
  * "Process & Ingest CSV" trigger button.
  * Monospaced Console Log Area displaying simulated ingestion metrics: Total Rows, Valid Rows, Invalid Rows, and the Delta Engine breakdown (Added, Modified, Removed, Unchanged).

### 8.10 `ReportsView.java`
* **Purpose**: Financial statement export.
* **Components**:
  * Consolidated dataset CSV exporter.
  * Native file save dialog (`FileChooser.showSaveDialog`) saving sanitized transaction ledgers to any user-selected path on disk.

### 8.11 `SettingsView.java`
* **Purpose**: Visual, profile, and algorithmic parameter configuration.
* **Controls**:
  * **Shopkeeper Mobile Number** (`TextField`): Configures the business phone number used as the sender contact and for receiving dual-party notifications.
  * **Shopkeeper Email Address** (`TextField`): Configures the business email address used for transaction copy receipts.
  * Visual Theme dropdown (`arctic-ledger`, `forest-ledger`, `copper-desk`, `ocean-glass`, `paper-ink`). Selecting a theme immediately updates the active window styling.
  * Minimum Safe Cash Threshold (`TextField`): Triggers warning alerts when projected cash falls below this level.
  * Default Forecast Days dropdown (`7`, `30`, `60`, `90`).
  * Currency Code (`INR`, `USD`, `EUR`, `GBP`).
  * "Save Settings" button with status confirmation.

### 8.12 `CommunicationView.java`
* **Purpose**: Dedicated customer outreach hub, multi-channel direct communication, and unified communication operations command center.
* **Layout**: A vertical container (`VBox`) inside a responsive `ScrollPane` split into executive outreach telemetry, direct directory outreach, and audit ledger tabs.
* **Telemetry Metric Cards (6x1 Grid)**:
  1. *Overdue Accounts*: Count of customers past their payment deadlines.
  2. *Total Overdue*: Outstanding rupee amount awaiting recovery.
  3. *Active Debtors*: Total customers with unpaid balances.
  4. *Phone Calls Logged*: Count of outbound phone calls recorded.
  5. *SMS Dispatched*: Total automated and manual text notices sent.
  6. *Emails Delivered*: Total digital invoices and payment links sent.
* **Direct Outreach Controls & Tabbed Workspaces**:
  * **Directory & Outreach Tab**:
    * `TextField searchField`: Real-time filter across customer name, mobile number, and email.
    * `ComboBox<String> statusFilter`: Filter by `ALL`, `OVERDUE ONLY`, or `PENDING`.
    * `Button btnBulkNotify`: "🚨 Alert All Defaulters" triggering one-click batch multi-channel notices.
    * `TableView<CustomerContact>`: Interactive directory displaying Customer Name, Mobile Number, Email Address, Outstanding Balance, Payment Status, and Action Buttons (📞 Call, 💬 SMS, ✉️ Email, 🔄 Dual-Notify).
  * **Communication & Audit History Tab**:
    * `TableView<CommunicationLog>`: Complete immutable log displaying Timestamp, Customer Name, Mobile/Email Target, Channel (`CALL`, `SMS`, `EMAIL`, `BOTH`), Dispatch Status (`INITIATED`, `DELIVERED`, `SENT`), and Message Preview.
* **Interactive Dialogs**:
  * `CallDialog`: Direct phone dialer modal showing the customer's phone number, outstanding invoice amount, delay days, and direct action to initiate the call and log shopkeeper audit notes.
  * `SmsDialog`: Formatted SMS preview modal with live character counter, pre-filled rupee balance, payment deadline, and secure UPI payment link.
  * `EmailDialog`: Official digital invoice and statement modal with subject line, message body, itemized billing summary, and sender verification.

---

## 9. The CSS Theming Engine & Visual Design

### 9.1 How `ThemeManager.java` Works
JavaFX applications style their controls using CSS files. `ThemeManager` allows instant, flicker-free theme switching at runtime without restarting the application or recreating views:

```java
public class ThemeManager {
    private static String currentTheme = "arctic-ledger";

    public static void applyTheme(Scene scene, String themeName) {
        if (themeName == null || themeName.trim().isEmpty()) {
            themeName = "arctic-ledger";
        }
        currentTheme = themeName;
        scene.getStylesheets().clear();

        String cssFile = "/styles/theme-" + themeName.toLowerCase() + ".css";
        URL resource = ThemeManager.class.getResource(cssFile);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            // Fallback to default
            URL fallback = ThemeManager.class.getResource("/styles/theme-arctic-ledger.css");
            if (fallback != null) {
                scene.getStylesheets().add(fallback.toExternalForm());
            }
        }
    }
}
```

### 9.2 The 5 Built-in Visual Palettes
Each theme is stored in `source/main/resources/styles/`:

| Theme Name | Visual Tone | Base Background | Accent Color | Ideal Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **`arctic-ledger`** | High-tech fintech dark mode | `#0f172a` (Slate Navy) | `#38bdf8` (Cyan Blue) | Default; modern analytics desks |
| **`forest-ledger`** | Deep organic wealth dark mode | `#0a1c14` (Deep Pine) | `#34d399` (Mint Green) | Calming, sustainable finance focus |
| **`copper-desk`** | Warm executive dark mode | `#1c140e` (Dark Bronze) | `#fb923c` (Copper Amber) | Traditional ledger feel with warm hues |
| **`ocean-glass`** | Luminous glassmorphism dark | `#091b2c` (Deep Ocean) | `#22d3ee` (Turquoise) | High-contrast data visualization |
| **`paper-ink`** | Clean editorial light mode | `#f8fafc` (Ivory White) | `#2563eb` (Royal Blue) | Daytime accounting, print-friendly presentation |

### 9.3 Semantic Colors & CSS Variables
All stylesheets define identical semantic variable names in `.root`, ensuring every view automatically inherits the active theme:
* `-color-bg-base`: Window background.
* `-color-bg-surface`: Sidebar, card, and table background.
* `-color-border`: Subtle divider lines between panes and cards.
* `-color-text-primary`: High-contrast typography for headers and numbers.
* `-color-text-secondary`: Muted typography for labels and metadata.
* `-color-accent`: Highlight color for active tabs, primary buttons, and selected items.
* `-color-success`: Green (`#10b981`) for positive cash flow and paid status.
* `-color-warning`: Amber (`#f59e0b`) for moderate warnings and pending items.
* `-color-danger`: Red (`#ef4444`) for critical deficits and overdue receivables.

---

## 10. How to Build, Run & Package

### Prerequisites
* **Java Development Kit (JDK)**: Version 21 or higher. Verify with:
  ```bash
  java -version
  ```
* **Apache Maven**: Version 3.8 or higher. Verify with:
  ```bash
  mvn -version
  ```

### Running the Application in Development Mode
Navigate to the `frontend-only` directory and run:
```bash
cd "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/frontend-only"

# Compile and start the desktop window
mvn javafx:run
```

### Clean Rebuild
```bash
mvn clean compile
```

### Packaging into a Standalone JAR
To package the application into a distribution JAR:
```bash
mvn clean package
```
The compiled artifact will be generated in `frontend-only/target/cashflow-frontend-only-1.0.0.jar`.

---

## 11. Backend Integration Guide (Connecting Real APIs)

When you are ready to connect this frontend to a real backend (e.g., Spring Boot REST API, Node.js, Go, or Python FastAPI), **you do not need to change any UI views or stylesheets**.

### Step 1: Create a REST Implementation of `CashFlowUiService`
Create a new class, for example `HttpBackendUiService.java`, that implements `CashFlowUiService` using Java 21's `java.net.http.HttpClient`:

```java
package com.cashflow.ui.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.dto.DashboardMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpBackendUiService implements CashFlowUiService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:8080/api";

    @Override
    public DashboardMetrics getDashboardMetrics() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/dashboard"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response.body(), DashboardMetrics.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new DashboardMetrics(); // fallback
        }
    }

    // Implement remaining methods to call /api/transactions, /api/forecast, etc.
}
```

### Step 2: Swap the Service in `StandaloneFrontendApp.java`
Replace `new MockUiService()` with your new implementation:

```java
@Override
public void start(Stage primaryStage) {
    // Switch from Mock to HTTP Backend:
    CashFlowUiService service = new HttpBackendUiService();

    MainLayout root = new MainLayout(service);
    Scene scene = new Scene(root, 1200, 800);
    // ... rest of start method remains identical
}
```

Every chart, table, filter, and theme will now render real backend data with zero changes to the presentation code!

---

## 12. Summary Checklist for Evaluating the Project

When discussing or reviewing this codebase with ChatGPT or technical interviewers:
1. **Separation of Concerns**: UI components (`com.cashflow.ui.view`) handle only layout, controls, and event listeners; business logic and data generation remain entirely behind `CashFlowUiService`.
2. **Defensive UI**: All amounts use `java.math.BigDecimal` with localized Indian Rupee (`₹`) formatting (`NumberFormat.getCurrencyInstance(new Locale("en", "IN"))`), avoiding floating-point rounding errors.
3. **Responsive Controls**: Tables utilize `CONSTRAINED_RESIZE_POLICY` so columns adapt smoothly to window resizing.
4. **Dynamic Styling**: Stylesheets are completely decoupled from Java code, making it trivial to add custom brand themes.
5. **Zero External Footprint**: The application runs completely in-memory, requiring no database installation, no migrations, and no internet connection.
