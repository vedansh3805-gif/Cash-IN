# CashFlow Architecture & Technical Map

## 1. Directory Structure

```
CASH_IN/
├── source/
│   └── main/
│       ├── java/com/cashflow/
│       │   ├── CashFlowApplication.java         # Spring Boot Server Entry Point
│       │   ├── controller/                      # REST API Endpoints
│       │   │   ├── AnomalyController.java       # Statistical anomaly radar API
│       │   │   ├── CommunicationController.java # Multi-channel customer outreach API
│       │   │   ├── CsvImportController.java     # CSV upload & demo dataset switcher
│       │   │   ├── DashboardController.java     # Aggregated liquidity telemetry
│       │   │   ├── ExpenseController.java       # Operating spend & recurring commitments
│       │   │   ├── ForecastController.java      # Forward liquidity trajectory (7-90 days)
│       │   │   ├── ReceivablesController.java   # Debtor aging & payment delay analysis
│       │   │   ├── ReportController.java        # Comprehensive audit statement generator
│       │   │   ├── ScenarioController.java      # What-If stress test simulation API
│       │   │   ├── SettingsController.java      # Business entity & threshold configuration
│       │   │   ├── TransactionController.java   # Filterable transaction ledger API
│       │   │   └── VersionController.java       # Version lineage & differential comparison
│       │   ├── model/                           # Domain Entities & Value Objects
│       │   │   ├── CommunicationLog.java        # Immutable call/SMS/email audit log
│       │   │   ├── CustomerContact.java         # Customer entity with phone/email
│       │   │   ├── DataChange.java              # Version delta record (ADDED, MODIFIED, REMOVED)
│       │   │   ├── DatasetVersion.java          # Dataset ingestion version metadata
│       │   │   ├── ForecastEntry.java           # Daily closing cash trajectory projection
│       │   │   ├── RecurringPayment.java        # Scheduled expense commitments
│       │   │   ├── Transaction.java             # Core double-entry transaction record
│       │   │   └── Warning.java                 # Early warning alert record
│       │   ├── repository/                      # Spring Data Repositories & JDBC DAOs
│       │   └── service/                         # Financial Domain Logic
│       │       ├── AnomalyService.java          # Z-score & 3x spending spike detection
│       │       ├── CashFlowService.java         # Runway math, KPIs, and liquidity metrics
│       │       ├── CommunicationService.java    # Multi-channel outreach & audit trail engine
│       │       ├── CsvImportService.java        # Resilient CSV parser & validation engine
│       │       ├── CsvValidationService.java    # Data sanitization, formats & phone verification
│       │       ├── DataComparisonService.java   # Automated version delta computation
│       │       ├── ForecastService.java         # Monte Carlo & deterministic cash projection
│       │       ├── ReceivablesService.java      # Aging schedule & payment delay profiling
│       │       └── WarningService.java          # Cash breach & liquidity risk engine
│       └── resources/
│           ├── application.properties           # Spring Boot & SQLite configuration
│           ├── dataset-1-baseline.csv           # Demo Dataset V1: Baseline Q3 Operations
│           ├── dataset-2-expansion.csv          # Demo Dataset V2: Rapid Expansion
│           ├── dataset-3-stress.csv             # Demo Dataset V3: Liquidity Stress Crisis
│           └── static/                          # High-Performance Web Frontend
│               ├── index.html                   # Single-Page Application Shell
│               ├── css/                         # Clean Enterprise Styling
│               │   ├── themes.css               # 5 Corporate Theme Palettes (CSS Variables)
│               │   ├── layout.css               # Zero-scroll executive grid & responsive shell
│               │   ├── components.css           # Data tables, badges, modals, KPI tiles
│               │   └── pages.css                # Page-specific views & widgets
│               └── js/                          # Modular Vanilla JavaScript
│                   ├── api.js                   # Unified REST API client
│                   ├── app.js                   # Application controller & routing
│                   ├── charts.js                # Offline Enterprise Canvas Chart Engine
│                   ├── communication.js         # Outreach hub, dialer, SMS/email modals
│                   ├── dashboard.js             # Zero-scroll dashboard telemetry
│                   ├── expenses.js              # Expense categories & recurring commitments
│                   ├── forecast.js              # Horizon switcher & trajectory charts
│                   ├── receivables.js           # Aging schedule & payment delay radar
│                   ├── reports.js               # Statement generation & CSV export
│                   ├── settings.js              # Business profile & theme management
│                   ├── timeline.js              # Transaction chronology stream
│                   ├── transactions.js          # Searchable, filterable ledger table
│                   ├── utils.js                 # Currency formatting, dates, toast notifications
│                   ├── versions.js              # Version comparison & delta inspector
│                   ├── warnings.js              # Early warning alerts & anomaly tab
│                   └── whatif.js                # What-If shock simulation engine
├── frontend-only/                               # Independent Desktop JavaFX Application
├── Explain.md                                   # Comprehensive Desktop Frontend Guide
├── DECISIONS.md                                 # Chronological Architecture Decision Log
├── ARCHITECTURE.md                              # This System Map Document
├── USER_GUIDE.md                                # Comprehensive Operational User Guide
└── pom.xml                                      # Maven Build & Dependency Configuration
```

---

## 2. Core Architectural Patterns

### A. Modular Single-Page Web Frontend (Vanilla JS + HTML5 Canvas)
- **Zero Build Toolchain**: No Node.js, Webpack, Babel, or npm dependencies required.
- **State Management**: Scoped module namespaces (`DashboardPage`, `ReceivablesPage`, `CommunicationPage`, `SettingsPage`) coordinate view-level state and communicate through the centralized `API` object.
- **Self-Contained Canvas Chart Engine**: `Charts.create(id, type, config)` implements 2D canvas drawing for bar, line, and doughnut charts, handling high-DPI displays (`window.devicePixelRatio`), theme color updates, and dashed safety baseline thresholds.

### B. High-Reliability Dual-Party Outreach Flow
1. User clicks **Call**, **SMS**, or **Email** on any overdue invoice or debtor card.
2. Modals pre-populate customer contact information (phone, email, outstanding balance).
3. Upon dispatch:
   - Client sends payload to `POST /api/communication/call`, `/sms`, or `/email`.
   - `CommunicationService` persists two audit entries:
     - Record 1: Target recipient (`BUYER`) with recipient phone/email.
     - Record 2: Shopkeeper copy (`SHOPKEEPER`) with shopkeeper phone/email configured in Settings.
   - An immutable audit trail is saved in SQLite `communication_log` with timestamp, channel, and delivery status.

### C. Versioning and Change Tracking
- Every dataset import generates a new `dataset_version` record.
- `DataComparisonService` compares the new version against the previous baseline:
  - Tracks added transaction records.
  - Identifies modified records (amount changes, status transitions from `PENDING` to `PAID`).
  - Flags removed records.
- Results are queried via `/api/versions/{id}/changes` and `/api/versions/compare?v1=...&v2=...`.
