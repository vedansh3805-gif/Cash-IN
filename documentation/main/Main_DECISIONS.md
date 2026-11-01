# Architectural Decision Record (ADR) — `DECISIONS.md`

### Decision 1: Pure Java Desktop UI with JavaFX (Java 21)
* **Date**: 2026-09-29
* **Decision**: Adopt native JavaFX for the user interface across both the Complete Project and the Independent Frontend, completely eliminating HTML and JavaScript.
* **Why Chosen**: Strictly fulfills the user requirement for a pure Java application matching the original baseline specifications in `project_draft.md`. Native desktop execution provides instant rendering, strong typing, and direct component interaction.
* **Alternatives Considered**: Web frontend (HTML/CSS/JS) or Swing. JavaFX was chosen over Swing for its superior charting (`LineChart`, `BarChart`, `PieChart`), modern CSS theming, and layout engine.
* **Impact**: All presentation views (`DashboardView`, `TransactionsView`, `ForecastView`, etc.) are written in pure Java.

### Decision 2: Decoupled UI Service Boundary (`CashFlowUiService`)
* **Date**: 2026-09-29
* **Decision**: Introduce a common interface `CashFlowUiService` between the presentation layer and the backend engine.
* **Why Chosen**: Allows the exact same JavaFX views, layouts, and CSS themes to run in two completely distinct modes:
  1. Connected to Spring Boot + SQLite (`BackendUiService.java`).
  2. Running standalone with mock data and zero backend/database dependencies (`MockUiService.java`).
* **Alternatives Considered**: Direct controller injection. Discarded because it tightly couples views to backend services, preventing standalone frontend extraction.
* **Impact**: `frontend-only/` contains zero database drivers, zero Spring Boot classes, and zero server network code.

### Decision 3: Five Persistent JavaFX CSS Themes
* **Date**: 2026-09-29
* **Decision**: Implement all 5 custom themes (*Arctic Ledger*, *Forest Ledger*, *Copper Desk*, *Ocean Glass*, *Paper & Ink*) using JavaFX CSS stylesheets.
* **Why Chosen**: Guarantees high-contrast readability, accessible focus rings, and professional styling across all native charts and table cells.
* **Impact**: Themes are dynamically swapped at runtime via `ThemeManager.applyTheme()`.

### Decision 4: Safe What-If Simulation
* **Date**: 2026-09-29
* **Decision**: Execute What-If scenarios purely in memory against a cloned copy of baseline forecast entries.
* **Why Chosen**: Guarantees that hypothetical stress tests never alter or corrupt actual transaction records in the SQLite database.
* **Impact**: Business owners can simulate delayed customer payments or expense surges with zero risk to historical data.

### Decision 5: Multi-Channel Communication Engine (Call, SMS, Email)
* **Date**: 2026-10-03
* **Decision**: Implement a dedicated `CommunicationService` with support for outbound phone dialer integration, SMS payment reminders, and itemized billing emails.
* **Why Chosen**: Small business shopkeepers operating on thin working capital cannot wait for customers to self-remit overdue invoices. Providing one-click Call, SMS, and Email actions directly from the Receivables dashboard drastically cuts recovery cycles.
* **Alternatives Considered**: Direct external API dependence (e.g. Twilio/SendGrid hard requirements). Discarded to maintain 100% offline desktop operability; the system uses a pluggable simulated provider by default with complete logging into `communication_log`.
* **Impact**: Added `CommunicationService`, `CommunicationController`, `CommunicationLogRepository`, and JavaFX modals (`CallDialog`, `SmsDialog`, `EmailDialog`).

### Decision 6: Dual-Party Notification Architecture
* **Date**: 2026-10-03
* **Decision**: Support simultaneous dispatch of collection alerts to both ends of the party (Buyer and Shopkeeper).
* **Why Chosen**: In financial recovery, shopkeepers need immediate verification that an alert was dispatched with the exact timestamp and amount, while the buyer receives the actionable reminder. This ensures complete transparency, audit compliance, and dispute prevention.
* **Alternatives Considered**: Buyer-only notification. Discarded because shopkeepers would lack confirmation of automated or manual notices without checking internal logs.
* **Impact**: Added `notifyBothParties` method in `CommunicationService`, dual-party logging in `communication_log`, and a dedicated "Notify Both" action button in `ReceivablesView`.

### Decision 7: Backward-Compatible CSV Contact Ingestion & Regex Validation
* **Date**: 2026-10-03
* **Decision**: Enhance `CsvValidationService` and `CsvImportService` to dynamically detect contact headers (`customer_phone`, `buyer_mobile`, `customer_email`, `buyer_email`) and validate them using regex while treating them as gracefully optional.
* **Why Chosen**: Users upload legacy CSV datasets that do not have phone or email columns, or rows representing general operational expenses that have no customer. The parser must remain completely backward compatible while enforcing valid formatting whenever contacts are present.
* **Alternatives Considered**: Requiring phone and email strictly on all rows. Discarded because it breaks existing sample datasets and expense records.
* **Impact**: Existing datasets continue to import cleanly; datasets with contacts populate buyer profiles and enable direct communication actions.

### Decision 8: Automated Non-Destructive Database Schema Evolution
* **Date**: 2026-10-03
* **Decision**: Implement an automated schema migration runner inside `DatabaseConfig.java` that checks for missing columns and applies safe `ALTER TABLE ADD COLUMN` statements and `CREATE TABLE IF NOT EXISTS communication_log`.
* **Why Chosen**: Avoids corrupting or dropping existing SQLite databases (`cashflow.db`) upon application upgrades, while ensuring all new columns (`customer_phone`, `customer_email`, `shopkeeper_phone`, `shopkeeper_email`) and tables are initialized automatically on startup.
* **Alternatives Considered**: Dropping and recreating tables on launch or manual SQL scripts. Discarded to prevent accidental loss of user datasets.
* **Impact**: Safe, zero-downtime database upgrades across development and production desktop environments.

### Decision 9: Full Recurring Obligation Management, Cumulative Scenario Math & Live Statement Generation
* **Date**: 2026-10-26
* **Decision**: Expand `ExpensesView` with in-app creation and deletion of scheduled recurring commitments, refine `ScenariosView` & `BackendUiService` with mathematically continuous rolling cash compounding across all 5 shock types, enable dynamic version inspection in `VersionsView`, and provide an executive audit statement generator in `ReportsView`.
* **Why Chosen**: Enables full operational autonomy directly within the desktop GUI without needing external SQL or API tools. Ensures What-If simulations accurately track compounding day-over-day cash flow deficits.
* **Alternatives Considered**: Static simulation without cumulative day-over-day rolling calculations. Discarded because compound liquidity drain must accurately reflect across the 90-day forecast horizon.
* **Impact**: UI views (`ExpensesView`, `ScenariosView`, `VersionsView`, `ReportsView`) and backend services (`BackendUiService`, `MockUiService`) provide seamless, end-to-end functionality across both the complete backend version and the standalone frontend-only version.

### Decision 10: Dedicated Customer Communication Center & Browser-Ready Web Application
* **Date**: 2026-10-27
* **Decision**: Introduce a dedicated **Communication Page** (`CommunicationView.java` in JavaFX and `#page-communication` / `communication.js` in the Web App) displaying customer mobile numbers and email addresses with interactive call/sms/email modals, dual-party audit dispatch, and bulk defaulter alerts; simultaneously complete the browser-based Web Application (`source/main/resources/static/`) providing a fully-functional 11-page responsive interface.
* **Why Chosen**: Small business owners requested clear visibility of customer mobile numbers and email addresses in a dedicated outreach view to initiate calls, send automated SMS reminders, and dispatch formal billing emails without navigating away from their cash management workflow. Deploying the browser-based web application ensures the platform is accessible on mobile, tablet, and desktop browsers alongside the native JavaFX desktop client.
* **Alternatives Considered**: Keeping communication actions confined strictly to a sub-modal within Receivables. Discarded because customer relationship management and overdue debt recovery require dedicated audit tracking and prominent contact directories.
* **Impact**: Both the JavaFX desktop application and the Spring Boot web application provide a dedicated Communication Page, complete dual-party audit tracking, click-to-dial and click-to-email integration, and 100% feature parity.

### Decision 11: Native Zero-Dependency HiDPI HTML5 Canvas Chart Engine (`EnterpriseChart`)
* **Date**: 2026-10-27
* **Decision**: Replace external CDN Chart.js dependencies with a native, zero-dependency, HiDPI Retina-sharp HTML5 Canvas Chart Engine (`EnterpriseChart` implemented directly in `charts.js`).
* **Why Chosen**: In offline, isolated, or restricted corporate networks, external CDN scripts (`https://cdn.jsdelivr.net/...`) fail to load or get blocked by firewalls/CSP, rendering all financial charts blank. Creating an internal canvas engine guarantees 100% offline operability, zero external network requests, sub-millisecond initialization, and full support for interactive tooltips, dashed safety thresholds, dual-series trajectories, and high-DPI crisp rendering.
* **Alternatives Considered**: Bundling local minified `chart.js` vendor file. Discarded because the custom lightweight canvas engine (`EnterpriseChart`) specifically satisfies the application's exact financial charting formats (Line, Bar, Horizontal Bar, Doughnut) with zero third-party vulnerability footprint and sub-10KB payload.
* **Impact**: All charts across Dashboard, Forecast, Receivables, Expenses, Versions, and Scenarios render instantly and reliably with zero external dependencies.

### Decision 12: True Differential Version Comparison Engine
* **Date**: 2026-10-27
* **Decision**: Implement a bidirectional, transaction-keyed differential comparison engine in `VersionController.java`, `BackendUiService.java`, and `versions.js`.
* **Why Chosen**: Previously, version comparisons queried a single version's change log or compared identical snapshots, reporting no differences between versions. Small business auditors require accurate deltas between any arbitrary versions $v_1$ and $v_2$, matching records by `transaction_id` to accurately report counts of `added`, `modified`, `removed`, and `unchanged` transactions, alongside granular field-level diffs (`amount`, `due_date`, `status`, etc.).
* **Alternatives Considered**: Line-by-line raw text diffing. Discarded because CSV row ordering changes would falsely report modified records rather than true entity-level field mutations.
* **Impact**: Accurate, transparent differential comparisons with visual breakdown charts across all historical dataset snapshots.

### Decision 13: Professional ERP SaaS UI/UX Architecture & Zero-Emoji Design System
* **Date**: 2026-10-27
* **Decision**: Perform an enterprise-grade UI/UX makeover eliminating all consumer emojis across the web application and JavaFX desktop interface, replacing them with crisp SVG vector icons, breadcrumb navigation, live pulse indicators, tabular financial numerals, and clean status badges.
* **Why Chosen**: The user requested a clean, professional, enterprise-grade ERP SaaS dashboard suitable for executive presentation and commercial deployment. Consumer emojis detract from the seriousness of financial risk and cash-flow management; replacing them with sleek SVG iconography and structured SaaS layouts delivers an appealing, intuitive, and modern experience.
* **Alternatives Considered**: Retaining emojis as secondary decorators. Discarded to achieve a strictly professional, high-density ERP SaaS standard.
* **Impact**: Modern, distraction-free visual design across all 11 modules with SVG icons, breadcrumbs, status indicators, and clean typography.

### Decision 14: Compact Zero-Scroll Executive Dashboard, Multi-Horizon Dynamic Projections & Pre-Loaded Demo Datasets
* **Date**: 2026-10-28
* **Decision**: 
  1. Re-architect the Dashboard into a compact 100vh single-viewport executive SaaS layout with an 8-metric horizontal strip and a 2-column balanced grid, eliminating vertical page scrolling completely on `#page-dashboard`.
  2. Anchor `ForecastService` projections from the active transition date between realized and pending records, and integrate ongoing periodic recurring obligations (`DAILY`, `WEEKLY`, `MONTHLY`) across the 90-day horizon.
  3. Create and seed 3 comprehensive demo datasets (`dataset-1-baseline.csv`, `dataset-2-expansion.csv`, `dataset-3-stress.csv`) with a 1-click Quick Demo Switcher (`/api/csv/demo/{name}`) and enable version-specific filtering across all APIs (`getDashboard`, `getForecast`).
* **Why Chosen**: Users requested a clean zero-scroll dashboard where all 8 KPIs, 3 analytics charts, upcoming commitments, and risk radars are instantly visible without scrolling. Furthermore, previous sample data was confined to 10 historical rows, causing 7, 30, 60, and 90-day horizons to appear identical. Distributing transactions and recurring obligations across the full 90-day window enables 7d, 30d, 60d, and 90d toggles to compute distinct cash floors, buffers, and dynamic curves, while distinct versions demonstrate real differential comparison deltas.
* **Impact**: Single-screen zero-scroll dashboard, fully dynamic multi-horizon forecasting, and instant 1-click demo dataset switching across baseline, expansion, and crisis scenarios.




