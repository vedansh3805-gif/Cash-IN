# Small Business Cash-Flow Early Warning System
## Project Draft

> **Purpose:** This document is the implementation baseline for the Java desktop application. It intentionally focuses on features and decisions needed to build and demonstrate the project, while removing repetitive implementation detail.

---

# 0. Developer Documentation Requirement

During development, maintain **three separate documents** alongside the project. These documents must be updated continuously, not created only at the end.

### Document 1 — `DECISIONS.md`
Maintain a chronological record of **relevant development decisions**.

For every meaningful decision, record:
- Date
- Decision
- What was changed/chosen
- Why it was chosen
- Alternatives considered
- Impact on the project

Examples:
- Why SQLite was selected instead of MySQL.
- Why a particular forecasting method was selected.
- Why a UI component or library was changed.
- Why a database/table structure was designed in a particular way.
- Why a feature was implemented or excluded.

The purpose is to make it possible to understand **what happened, why it happened, and what effect the decision had**.

### Document 2 — `ARCHITECTURE.md`
Maintain a clear technical map of the project.

It must explain:
- Complete folder structure
- Purpose of every major folder
- Purpose of important files/classes
- Responsibilities of controllers, services, models and database components
- Data flow between major components
- Database tables and relationships
- Where a developer should modify the code for a particular feature

The document should be understandable to a programmer who joins the project later.

### Document 3 — `USER_GUIDE.md`
Maintain a practical guide explaining **how the finished application works**.

It must cover:
- How to launch the application
- Initial setup
- CSV upload
- CSV requirements
- Dashboard
- Filters
- Transactions
- Receivables
- Expenses
- Forecast
- Warnings
- Anomalies
- What-If Simulator
- Dataset versions
- Comparison
- Reports/export
- Themes/settings
- Meaning of important indicators and warnings

A user should be able to run the application and understand its features using only this document.

**Rule:** Antigravity must update these three documents whenever the architecture, behaviour, or an important development decision changes.

---

# 1. Project Overview

The **Small Business Cash-Flow Early Warning System** is a Java desktop financial analytics application for small businesses.

The user periodically uploads financial data as a CSV. The application:

**Upload → Validate → Version → Compare → Update → Analyze → Forecast → Warn → Explain → Simulate → Report**

The system focuses on identifying potential future cash shortages rather than only showing historical bookkeeping data.

### Technology
- Java 21+
- JavaFX
- JDBC
- SQLite
- Maven
- OpenCSV or Apache Commons CSV
- JUnit 5
- Optional REST APIs

APIs must remain optional and must never be required for core functionality.

---

# 2. Problem Statement

Small businesses may have:
- Pending or overdue customer payments
- Upcoming salaries, rent and supplier payments
- Recurring expenses
- Delayed receivables
- Unexpected expense increases
- A healthy current balance but insufficient future liquidity

The application should answer:

1. **What changed?**
2. **What does the change mean?**
3. **What could happen next?**

---

# 3. Core Objectives

1. Import financial data from CSV.
2. Validate and clean imported data.
3. Preserve historical CSV versions.
4. Detect added, modified, removed and unchanged transactions.
5. Track income, expenses and receivables.
6. Track recurring payments.
7. Analyze customer payment delays.
8. Forecast future cash position.
9. Detect unusual financial changes.
10. Generate explainable early warnings.
11. Provide interactive dashboards and reports.
12. Allow what-if financial scenarios.
13. Preserve a complete change history.

---

# 4. CSV Workflow

## First Upload

```text
CSV
 ↓
Validate structure and values
 ↓
Create dataset version
 ↓
Store valid transactions
 ↓
Calculate financial state
 ↓
Generate forecast
 ↓
Detect anomalies
 ↓
Generate warnings
 ↓
Display dashboard
```

### Example CSV (with Buyer Contact Details)

```csv
transaction_id,transaction_date,transaction_type,category,description,amount,customer,customer_phone,customer_email,supplier,due_date,status,payment_date
TX001,2026-06-01,INCOME,Sales,Product A Sales,150000.0,ABC Ltd,+91-98765-10001,billing@abcltd.com,,2026-06-15,PAID,2026-06-14
TX002,2026-06-02,EXPENSE,Rent,Office Rent June,50000.0,,,,Prime Estates,2026-06-05,PAID,2026-06-03
TX003,2026-06-05,INCOME,Sales,Product B Sales,200000.0,XYZ Corp,+91-98765-10002,accounts@xyzcorp.com,,2026-07-05,PENDING,
```

> **Contact Columns Support**: Datasets may include `customer_phone` (or `buyer_mobile`, `phone`, `mobile`) and `customer_email` (or `buyer_email`, `email`). When provided, the system links them to customer debt profiles for automated and manual multi-channel communications (Calls, SMS, Email).

## Subsequent Upload

A new CSV must **not overwrite the previous version**.

Transactions are matched using `transaction_id`.

| Result | Meaning |
|---|---|
| ADDED | Exists in new version only |
| MODIFIED | Exists in both but relevant data changed |
| REMOVED | Exists in previous version but not new version |
| UNCHANGED | No relevant change |

Historical records and changes must remain available.

---

# 5. CSV Validation

The application should validate:
- Required headers (`transaction_id`, `transaction_date`, `transaction_type`, `category`, `description`, `amount`, `status`)
- Transaction ID uniqueness and format
- Date format (`YYYY-MM-DD`)
- Amount (positive decimal values)
- Transaction type (`INCOME` / `EXPENSE`)
- Status (`PAID`, `PENDING`, `OVERDUE`)
- Due date where applicable
- Duplicate IDs
- Contact fields validation:
  - Phone format: valid telephone number patterns (supports E.164, international, and regional formatted numbers like `+91-98765-10001` or `9876543210`)
  - Email format: valid RFC 5322 email patterns (`user@domain.com`)
  - Contact fields are gracefully optional; if blank, the transaction is ingested without contact info
- Empty/invalid values
- Corrupted files

Invalid rows must be reported clearly instead of silently ignored.

Example:

```text
Total rows: 250
Valid rows: 244
Invalid rows: 6

Added: 32
Modified: 14
Removed: 3
Unchanged: 195
```

---

# 6. Dashboard

The dashboard is the primary screen.

### KPI Cards
- Current Cash
- Expected Incoming Cash
- Upcoming Expenses
- Pending Receivables
- Overdue Receivables
- Net Cash Flow
- Forecasted Minimum Cash
- Risk Status

### Main Dashboard Areas

```text
┌──────────────────────────────────────────────┐
│ Current Cash | Receivables | Expenses | Risk │
├──────────────────────────────────────────────┤
│              Cash Flow Chart                 │
├──────────────────────────┬───────────────────┤
│ Forecast / Inflow-Outflow│ Upcoming Payments │
├──────────────────────────┼───────────────────┤
│ Warnings & Anomalies     │ Recent Changes    │
└──────────────────────────┴───────────────────┘
```

The dashboard should prioritize **important financial information first**, not decorative elements.

---

# 7. Early Warning System

A warning is generated when projected cash falls below the configured minimum safe cash threshold.

### Warning Types
- Low projected cash
- Upcoming large expense
- Overdue receivable
- Customer payment delay risk
- Income drop
- Expense spike
- Forecast uncertainty

### Severity

```text
INFO
ATTENTION
WARNING
CRITICAL
```

Every warning must explain:

- What happened?
- Why does it matter?
- When could it matter?
- Which transactions contribute?
- What amount is involved?

Example:

```text
WARNING

Projected cash may fall below ₹50,000 on 25 June.

Main causes:
• ₹80,000 salary payment due
• ₹60,000 overdue customer invoice

Projected minimum cash: ₹38,000
Configured threshold: ₹50,000
```

Do not display unexplained scores such as `Risk Score: 82`.

---

# 8. Cash-Flow Forecast

Base calculation:

```text
Projected Cash =
Current Cash
+ Expected Inflows
- Expected Outflows
```

For each future date:

```text
Opening Cash
+ Expected Receipts
- Expected Expenses
= Closing Cash
```

Default forecast periods:
- 7 days
- 30 days
- 60 days
- 90 days

The user can select a custom period.

Historical customer payment delays may shift the expected payment date. Such dates must clearly be labelled as estimates.

---

# 9. Receivables Analysis

Track:
- Customer
- Invoice amount
- Invoice date
- Due date
- Payment date
- Payment status
- Days overdue
- Historical payment behaviour

Statuses:

```text
PAID
PENDING
DUE SOON
OVERDUE
HIGH-RISK DELAY
```

Useful metrics:
- Total outstanding
- Overdue amount
- Average payment delay
- Customer-level payment history

---

# 9.1 Multi-Channel Defaulter & Delay Outreach System

When customer payment delays occur or accounts turn into defaulters, the system empowers the shopkeeper to take immediate recovery action directly from the desktop interface across three distinct communication channels: **Phone Calls, SMS, and Email**, featuring **dual-party synchronization**:

### Multi-Channel Capabilities:
1. **Phone Call Trigger (`CALL`)**:
   - Initiates immediate outbound dialer to the customer's mobile number (`customer_phone`).
   - Displays buyer contact, pending invoice amount, overdue duration, and caller guidance script.
   - Logs call timestamps, agent details, and call outcomes into the permanent audit ledger.

2. **SMS Notification (`SMS`)**:
   - Dispatches formatted payment reminder text directly to buyer's phone.
   - Pre-populated concise template: *"Payment Reminder: Invoice [ID] for ₹[Amount] was due on [Date]. Please clear immediately."*
   - Sends real-time delivery confirmation.

3. **Email Notification (`EMAIL`)**:
   - Dispatches formal billing reminder and account statement directly to buyer's email address (`customer_email`).
   - Contains complete itemized invoice summary, due dates, penalty/delay status, and bank settlement instructions.

4. **Dual-Party Notification (`NOTIFY BOTH`)**:
   - **Crucial Requirement**: Every reminder or defaulter notice can be dispatched simultaneously to **both ends of the party**:
     - **Buyer End**: Receives the collection reminder with overdue details and payment link.
     - **Shopkeeper End**: Receives an instant verification copy (SMS/Email) confirming outreach transmission, ensuring transparent bookkeeping and audit readiness.

5. **Bulk Defaulters Outreach**:
   - Single-click action (`Alert All Defaulters`) that scans all accounts exceeding credit terms and dispatches batch notices across the portfolio.

6. **Communication Audit Trail**:
   - Every outreach attempt across all channels generates an immutable entry in `communication_log` with:
     - `log_id`, `transaction_id`, `customer_name`, `channel` (`CALL`, `SMS`, `EMAIL`, `BOTH`)
     - `recipient_type` (`BUYER`, `SHOPKEEPER`, `BOTH`)
     - `recipient_contact` (phone or email address)
     - `message_body`, `sent_at` timestamp, and `delivery_status`.

---

# 10. Expense Analysis

Expense categories may include:
- Rent
- Salaries
- Utilities
- Inventory
- Suppliers
- Marketing
- Taxes
- Loans
- Software/Subscriptions
- Other

The system should identify recurring expenses and include them in future forecasts.

---

# 11. Recurring Payments

Users can define recurring expenses such as:

```text
Office Rent
₹30,000
Monthly
Due on 5th
```

Future recurring payments must automatically affect forecasts.

Recurring income may also be supported.

---

# 12. Anomaly Detection

The system identifies unusual financial values or changes.

Examples:
- Expense suddenly much higher than normal
- Unusually large transaction
- Customer payment significantly later than usual
- Sudden income decrease
- Rapid category-level expense growth

Use explainable methods such as:
- Historical average comparison
- Percentage change
- Median deviation
- Interquartile range
- Z-score for sufficiently large datasets

An anomaly means **unusual and requiring attention**. It must not automatically be described as fraud.

---

# 13. What-If Simulator

Users can test hypothetical situations without changing real data.

Examples:
- Customer pays 15 days late
- Expenses increase by 10%
- Expected sales decrease by 20%
- Unexpected ₹1,00,000 expense occurs

Workflow:

```text
Actual Data
 ↓
Scenario Copy
 ↓
Apply Hypothetical Change
 ↓
Run Forecast
 ↓
Compare With Baseline
 ↓
Display Result
```

The actual database must never be modified by a scenario.

---

# 14. Dataset Versions & Comparison

Each upload creates a version:

```text
V1 — 01 Jun 2026
V2 — 13 Jun 2026
V3 — 25 Jun 2026
```

Users can view:
- Dataset state
- Transaction count
- Income
- Expenses
- Receivables
- Forecast
- Changes from previous version

### Before vs After

Show:

```text
Added       18
Modified     7
Removed      2
Unchanged  154
```

Users can filter changes by:
- Type
- Category
- Customer
- Supplier
- Change type
- Amount
- Date

A changed transaction should show its previous and current values.

---

# 15. Data Change Timeline

Display dataset evolution chronologically.

```text
01 JUN
V1 uploaded
       ↓
13 JUN
V2 uploaded
Changes detected
       ↓
25 JUN
V3 uploaded
Forecast recalculated
       ↓
CURRENT
Latest financial state
```

This provides a clear historical audit trail.

---

# 16. Interactive Charts

Charts should be **analytical tools**, not decoration.

### Required Charts

1. Cash Balance Over Time
2. Expected Inflow vs Outflow
3. Income vs Expense
4. Receivables Aging
5. Expense by Category
6. Monthly Cash Flow
7. Forecasted Cash Position
8. Customer Payment Delay
9. Dataset Change Summary

### Chart Behaviour

Charts should support:
- Hover tooltips with exact values
- Date filtering
- Category filtering where applicable
- Dataset/version comparison
- Click-to-filter where practical
- Clear legends
- Empty states
- Responsive resizing

### Design Rules

- Use line charts for time-based trends.
- Use grouped bars for comparisons.
- Use stacked bars only when composition matters.
- Use donut/pie charts sparingly.
- Use consistent semantic colours.
- Avoid excessive gradients, 3D effects and decorative animation.
- Highlight only the data point or series currently being inspected.

---

# 17. Interactive Data Tables

Tables should be designed for **financial investigation**, not simply data display.

### Required Capabilities
- Search
- Multi-column sorting
- Column filters
- Pagination
- Date filtering
- Status filtering
- Column visibility
- Row expansion
- Clear status indicators

### Transaction Table

Recommended columns:

```text
Date | Type | Category | Description | Amount |
Customer/Supplier | Due Date | Status | Version
```

### Expandable Row

Opening a transaction should reveal:

```text
Transaction Details
───────────────────
Current values
Previous values
Dataset version
Change detected
Payment history
Related warning/anomaly
```

### Table UX

- Freeze important columns where practical.
- Keep financial amounts right-aligned.
- Use consistent number/currency formatting.
- Keep headers visible while scrolling.
- Use pagination for large datasets.
- Provide clear empty, loading and error states.
- Never rely on colour alone for status.

---

# 18. Global Filters

Filters should affect relevant KPIs, charts and tables without restarting the application.

### Filters
- Date range
- Transaction type
- Category
- Customer
- Supplier
- Status
- Dataset version

### Date Presets

```text
Today
7 Days
30 Days
90 Days
This Month
Previous Month
Custom Range
```

---

# 19. Visual Theme System

Use **five professional themes**. Themes must maintain strong contrast and must not reduce readability of user-entered financial data.

### Theme 1 — Arctic Ledger
- Deep Navy
- Slate Blue
- Soft White
- Ice Blue
- Muted Teal

### Theme 2 — Forest Ledger
- Deep Forest
- Sage
- Warm Ivory
- Soft Mint
- Charcoal

### Theme 3 — Copper Desk
- Deep Charcoal
- Copper
- Warm Sand
- Cream
- Muted Terracotta

### Theme 4 — Ocean Glass
- Deep Ocean
- Teal
- Seafoam
- Pearl White
- Dark Slate

### Theme 5 — Paper & Ink
- Warm White
- Graphite
- Burgundy
- Muted Gold
- Warm Gray

### Theme Rules

Every theme must maintain:
- WCAG-conscious text contrast
- Readable input fields
- Clear table borders
- Distinguishable status indicators
- Consistent chart readability
- Accessible focus/selected states

Themes must affect the complete UI and persist between sessions.

Avoid:
- Generic purple/blue AI gradients
- Excessive neon
- Heavy glassmorphism
- Low-contrast text
- Decorative effects that interfere with data readability

---

# 20. Application Navigation

Use a **compact sidebar navigation** with a clear separation between daily analysis and configuration.

```text
┌─────────────────────────────┐
│ CASHFLOW                    │
│                             │
│ OVERVIEW                    │
│   Dashboard                 │
│                             │
│ ANALYZE                     │
│   Transactions              │
│   Receivables               │
│   Communication             │
│   Expenses                  │
│   Forecast                  │
│   Warnings & Anomalies      │
│                             │
│ INVESTIGATE                 │
│   Versions & Changes        │
│   Timeline                  │
│   What-If Simulator         │
│                             │
│ OUTPUT                      │
│   Reports & Export          │
│                             │
│ SYSTEM                      │
│   Settings                  │
└─────────────────────────────┘
```

### Navigation Rules

- Dashboard is the default landing page.
- CSV import should always be easily accessible.
- Active navigation must be visually obvious.
- Avoid excessive nested menus.
- Settings should contain business profile, threshold, currency and theme settings.
- Navigation must remain consistent across all screens.

---

# 21. Technical Architecture

Use:

**MVC + Service Layer + DAO/Repository**

```text
JavaFX View
     ↓
Controller
     ↓
Service Layer
     ↓
Model / Domain
     ↓
DAO / Repository
     ↓
SQLite
```

### Presentation
JavaFX screens, charts, tables and forms.

### Controllers
Handle UI events and navigation.

### Services
Contain business logic:
- CSV import
- Validation
- Comparison
- Cash flow
- Forecast
- Warnings
- Anomalies
- Reports
- Themes

### Model
Examples:
- Transaction
- DatasetVersion
- DataChange
- Customer
- RecurringPayment
- Forecast
- Warning
- Anomaly
- BusinessSettings
- Scenario

### DAO / Repository
Handle JDBC and database operations.

---

# 22. Database Design

SQLite is the primary database for the project.

### Main Tables

**business**
- business_id
- business_name
- minimum_cash_threshold
- created_at
- updated_at

**dataset_version**
- version_id
- version_number
- file_name
- upload_date
- record_count
- previous_version_id

**transaction**
- transaction_id
- version_id
- transaction_date
- transaction_type
- category
- description
- amount
- customer
- customer_phone
- customer_email
- supplier
- due_date
- payment_date
- status

**data_change**
- change_id
- version_id
- transaction_id
- change_type
- field_name
- old_value
- new_value
- changed_at

**customer**
- customer_id
- customer_name
- phone
- email
- average_payment_delay
- total_outstanding

**recurring_payment**
- recurring_id
- description
- category
- amount
- frequency
- next_due_date
- active

**forecast**
- forecast_id
- version_id
- forecast_date
- opening_cash
- expected_inflow
- expected_outflow
- closing_cash

**warning**
- warning_id
- version_id
- warning_type
- severity
- warning_date
- projected_date
- message
- status

**anomaly**
- anomaly_id
- version_id
- transaction_id
- anomaly_type
- severity
- expected_value
- actual_value
- explanation

**application_settings**
- setting_id
- theme
- forecast_days
- minimum_cash_threshold
- currency
- shopkeeper_phone
- shopkeeper_email

**communication_log**
- log_id
- transaction_id
- customer_name
- channel (CALL, SMS, EMAIL, BOTH)
- recipient_type (BUYER, SHOPKEEPER, BOTH)
- recipient_contact (phone/email)
- message_body
- sent_at
- status (CONNECTED, DELIVERED, SENT, FAILED)

Historical data must never be destroyed because a new CSV is uploaded.

---

# 23. Core Application Flow

```text
START
 ↓
Load configuration
 ↓
Connect to SQLite
 ↓
Load theme
 ↓
Load latest dataset
 ↓
Display dashboard
 ↓
User uploads CSV
 ↓
Validate
 ↓
Create version
 ↓
Compare with previous version
 ↓
Record changes
 ↓
Store data
 ↓
Recalculate metrics
 ↓
Generate forecast
 ↓
Detect anomalies
 ↓
Generate warnings
 ↓
Refresh UI
 ↓
User investigates
 ↓
Optional What-If scenario
 ↓
Optional Report/Export
```

---

# 24. Security & Error Handling

### Security
- Use prepared SQL statements.
- Keep database/configuration secrets outside source code.
- Validate uploaded file type and size.
- Never expose credentials in logs.
- Handle exceptions safely.
- Use secure password hashing if authentication is added.

### Error Categories
- File Error
- Validation Error
- Database Error
- Forecast Error
- API Error
- Unexpected Application Error

Show users actionable messages instead of raw Java exceptions.

---

# 25. Performance & Usability

- Do not freeze the JavaFX UI during CSV processing.
- Use background tasks for imports and heavy database operations.
- Use pagination for large tables.
- Index transaction ID, date and version ID.
- Avoid unnecessary recalculation.
- Use `BigDecimal` for monetary calculations.
- Provide loading and empty states.
- Support readable fonts and strong contrast.
- Provide tooltips and meaningful labels.
- Never use colour as the only status indicator.

---

# 26. Reports & Export

Reports should include:
- Selected date range
- Current cash
- Income
- Expenses
- Receivables
- Overdue payments
- Forecast
- Warnings
- Anomalies
- Dataset version

Support CSV export and PDF export if practical.

---

# 27. Four-Review Development Plan

## Review 1 — Foundation & Import

Build:
- JavaFX project
- Navigation shell
- Theme foundation
- SQLite connection
- Database schema
- CSV upload
- CSV validation
- Transaction storage
- Basic transaction table

Demonstration:

```text
CSV → Validate → SQLite → Transaction Table
```

## Review 2 — Versioning & Dashboard

Build:
- Dataset versions
- Change detection
- Added/modified/removed records
- Change history
- Dashboard
- KPI cards
- Interactive tables
- Basic charts
- Filters

Demonstration:

```text
V1 → V2 → Automatic Comparison → Dashboard Update
```

## Review 3 — Financial Intelligence

Build:
- Cash-flow calculation
- 7/30/60/90-day forecast
- Receivables analysis
- Recurring payments
- Payment-delay analysis
- Warning engine
- Anomaly detection
- Forecast visualization

Demonstration:

```text
Financial Data → Forecast → Risk Detection → Explainable Warning
```

## Review 4 — Final Product

Build:
- What-If Simulator
- Advanced comparison
- Timeline
- Reports/export
- Five themes
- Animations where useful
- Optional API integration
- Error handling
- Testing
- Performance improvements
- Final documentation

Final demonstration:

```text
UPLOAD
 → VALIDATE
 → VERSION
 → COMPARE
 → UPDATE
 → ANALYZE
 → FORECAST
 → WARN
 → EXPLAIN
 → SIMULATE
 → REPORT
```

---

# 28. Testing

### Unit Testing
Test:
- CSV parsing
- Validation
- Transaction comparison
- Forecast calculation
- Warning thresholds
- Anomaly calculations

### Integration Testing

```text
CSV → SQLite
SQLite → Forecast
Forecast → Warning
V1 → V2 → Change History
```

### UI Testing
Verify:
- Navigation
- CSV upload
- Filters
- Theme switching
- Table interactions
- Charts
- Warning display
- Report export

---

# 29. Core Domain Classes

Minimum:

```text
Transaction
DatasetVersion
DataChange
Customer
RecurringPayment
ForecastEntry
Warning
Anomaly
Business
ApplicationSettings
Scenario
```

Supporting:

```text
CsvImportResult
ValidationError
ChangeSummary
ForecastResult
WarningResult
AnomalyResult
DashboardMetrics
DateRange
```

---

# 30. Core Services

```text
CsvImportService
CsvValidationService
DataComparisonService
TransactionService
DatasetVersionService
CashFlowService
ForecastService
ReceivablesService
RecurringPaymentService
WarningService
AnomalyService
ScenarioService
ReportService
ThemeService
```

---

# 31. Important Functional Rules

1. Never silently overwrite historical dataset versions.
2. Never delete historical transactions because they are absent from a later CSV.
3. Every modification must be traceable.
4. Forecasts must be identified as estimates.
5. Warnings must explain their causes.
6. What-if scenarios must not modify real data.
7. API failure must not break core functionality.
8. Invalid CSV rows must be reported.
9. Database operations must use prepared statements.
10. Heavy processing must not block the JavaFX application thread.
11. Theme changes must apply consistently across the UI.
12. Use `BigDecimal` for monetary calculations.
13. Keep the system modular and explainable.

---

# 32. Definition of Done

The project is complete when the user can:

- Launch and configure the application.
- Select a theme.
- Upload a valid CSV.
- View imported transactions.
- Upload another CSV later.
- Automatically see what changed.
- View and compare dataset versions.
- View the change timeline.
- Use dashboard KPIs.
- Filter and explore charts.
- Search, sort and filter transactions.
- View receivables and overdue payments.
- View expenses and recurring payments.
- View 7/30/60/90-day forecasts.
- Receive an early warning before projected cash falls below the configured threshold.
- Understand why a warning occurred.
- Investigate anomalies.
- Run a what-if scenario.
- Generate/export a report.
- Restart the application without losing data.

---

# 33. Final Product Identity

The application should be positioned as:

> **A continuously updated cash-flow intelligence system for small businesses.**

It is not merely an expense tracker or CSV viewer.

Its core differentiator is:

```text
Historical CSV Data
        +
Automatic Change Detection
        +
Financial State Update
        +
Forecasting
        +
Anomaly Detection
        +
Early Warning
        +
Explainable Analysis
```

The final product should make the user's financial data understandable and help identify potential liquidity problems **before they become urgent**.

---

# 34. Build Priority

```text
1. Project + SQLite + JavaFX
        ↓
2. CSV Import + Validation
        ↓
3. Versioning + Change Detection
        ↓
4. Dashboard + Visualization
        ↓
5. Cash-Flow Forecast
        ↓
6. Warnings + Anomalies
        ↓
7. What-If Simulator
        ↓
8. Themes + Reports + UI Polish
        ↓
9. Testing + Optimization + Documentation
```

Do not build advanced features before the underlying data and versioning architecture is stable.

---

# 35. Implementation Rule

This document is the **single source of truth for implementation**.

The implementation must:
- Follow the architecture defined here.
- Keep UI, controllers, services, models and database access separated.
- Follow the four-review development sequence.
- Preserve historical CSV versions.
- Keep calculations explainable.
- Use `BigDecimal` for financial calculations.
- Keep APIs optional.
- Prioritize a stable working core before visual polish.
- Keep `DECISIONS.md`, `ARCHITECTURE.md` and `USER_GUIDE.md` continuously updated.
- Use realistic sample CSV data for demonstrations.
- Maintain clean, modular and documented Java code.

The complete product journey is:

```text
UPLOAD
→ VALIDATE
→ VERSION
→ COMPARE
→ UPDATE
→ ANALYZE
→ FORECAST
→ WARN
→ EXPLAIN
→ SIMULATE
→ REPORT
```
