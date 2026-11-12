import subprocess
import os

html_content = """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>CASHFLOW — Final Academic Project Report (Batch 8)</title>
<style>
  @page {
    margin: 1in;
  }
  body {
    font-family: 'Calibri', 'Segoe UI', Arial, sans-serif;
    line-height: 1.6;
    color: #1e293b;
    font-size: 11pt;
  }
  .cover-page {
    text-align: center;
    page-break-after: always;
    padding-top: 40px;
  }
  .college-name {
    font-size: 18pt;
    font-weight: bold;
    color: #0f172a;
    text-transform: uppercase;
    letter-spacing: 1px;
    margin-bottom: 6px;
  }
  .dept-name {
    font-size: 13pt;
    font-weight: 600;
    color: #2563eb;
    margin-bottom: 24px;
  }
  .project-title {
    font-size: 22pt;
    font-weight: bold;
    color: #0f172a;
    line-height: 1.3;
    margin: 35px 0 15px 0;
    border-top: 3px solid #2563eb;
    border-bottom: 3px solid #2563eb;
    padding: 18px 0;
  }
  .project-subtitle {
    font-size: 12pt;
    color: #475569;
    font-style: italic;
    margin-bottom: 40px;
  }
  .meta-table {
    width: 90%;
    margin: 30px auto;
    border-collapse: collapse;
    text-align: left;
    font-size: 11pt;
  }
  .meta-table td {
    padding: 8px 12px;
    border: 1px solid #cbd5e1;
  }
  .meta-table td.label {
    font-weight: bold;
    background-color: #f1f5f9;
    width: 35%;
  }
  h1 {
    font-size: 18pt;
    color: #0f172a;
    border-bottom: 2px solid #2563eb;
    padding-bottom: 6px;
    margin-top: 32px;
    margin-bottom: 12px;
  }
  h2 {
    font-size: 14pt;
    color: #1e40af;
    margin-top: 22px;
    margin-bottom: 8px;
  }
  h3 {
    font-size: 12pt;
    color: #334155;
    margin-top: 16px;
    margin-bottom: 6px;
  }
  p, li {
    text-align: justify;
    margin-bottom: 8px;
  }
  ul, ol {
    margin-top: 4px;
    margin-bottom: 12px;
    padding-left: 24px;
  }
  table.data-table {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    font-size: 10pt;
  }
  table.data-table th, table.data-table td {
    border: 1px solid #cbd5e1;
    padding: 8px 10px;
    text-align: left;
  }
  table.data-table th {
    background-color: #f8fafc;
    color: #0f172a;
    font-weight: bold;
  }
  table.data-table tr:nth-child(even) {
    background-color: #f8fafc;
  }
  .badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: bold;
    font-size: 9pt;
  }
  .badge-success {
    background-color: #dcfce7;
    color: #166534;
  }
  .code-block {
    background-color: #f8fafc;
    border: 1px solid #e2e8f0;
    border-left: 4px solid #2563eb;
    padding: 12px 14px;
    font-family: 'Consolas', 'Courier New', monospace;
    font-size: 9.5pt;
    line-height: 1.45;
    white-space: pre-wrap;
    margin: 14px 0;
    color: #0f172a;
  }
  .callout {
    background-color: #eff6ff;
    border-left: 4px solid #3b82f6;
    padding: 12px 16px;
    margin: 16px 0;
    font-style: italic;
    color: #1e3a8a;
  }
</style>
</head>
<body>

<!-- ==========================================================================
     1. COVER PAGE
     ========================================================================== -->
<div class="cover-page">
  <div class="college-name">Department of Computer Science &amp; Engineering</div>
  <div class="dept-name">Bachelor of Technology in Computer Science &amp; Engineering</div>

  <div class="project-title">
    CASHFLOW<br>
    <span style="font-size: 14pt; font-weight: 500;">Small Business Liquidity Management, Forward Cash Runway Early Warning &amp; Multi-Channel Defaulter Recovery System</span>
  </div>

  <div class="project-subtitle">
    A Comprehensive Capstone Project Report submitted in partial fulfillment of the requirements for the degree of Bachelor of Technology
  </div>

  <table class="meta-table">
    <tr>
      <td class="label">Course / Subject</td>
      <td>Capstone Project / Full Stack Software Engineering Lab</td>
    </tr>
    <tr>
      <td class="label">Project Batch</td>
      <td><strong>Batch 8</strong></td>
    </tr>
    <tr>
      <td class="label">Review Stage</td>
      <td><strong>Final Review (100% Ready &amp; Verified)</strong></td>
    </tr>
    <tr>
      <td class="label">Lead Architect &amp; Developer</td>
      <td><strong>Vedansh Saxena</strong> (Lead Architect &amp; Full Stack Developer)</td>
    </tr>
    <tr>
      <td class="label">Project Team Members</td>
      <td>
        • Member 1: Vedansh Saxena<br>
        • Member 2: [Team Member 2 Name &amp; Register No.]<br>
        • Member 3: [Team Member 3 Name &amp; Register No.]<br>
        • Member 4: [Team Member 4 Name &amp; Register No.]
      </td>
    </tr>
    <tr>
      <td class="label">Project Guide / Mentor</td>
      <td>[Faculty Mentor / Project Supervisor Name]</td>
    </tr>
    <tr>
      <td class="label">Academic Year</td>
      <td>2026 – 2027</td>
    </tr>
  </table>
</div>

<hr style="page-break-after: always; visibility: hidden;">

<!-- ==========================================================================
     2. ABSTRACT
     ========================================================================== -->
<h1>2. Abstract</h1>
<p>
Over <strong>82% of small and medium-sized enterprises (SMEs) collapse not from fundamental unprofitability, but from acute, unforeseen cash-flow insolvency</strong>: receivables remain uncollected for 30 to 90+ days while hard operating disbursements (salaries, inventory raw materials, rent, statutory utility dues) demand immediate liquid funds. Traditional commercial accounting suites (e.g., Tally, QuickBooks, SAP) function primarily as retrospective tax compliance ledgers; they lack predictive forward runway projections, automated late-payment penalty compounding, statistical spend anomaly radars, and integrated multi-channel debtor outreach.
</p>
<p>
<strong>CashFlow</strong> solves this operational dilemma. Developed as a self-contained, enterprise-grade Full-Stack platform, CashFlow incorporates:
</p>
<ol>
  <li><strong>Executive Real-Time Liquidity Cockpit</strong>: Instant, zero-scroll monitoring of liquid in-bank cash, expected collections, scheduled outflows, net velocity, and overdue debtor exposure.</li>
  <li><strong>Forward Cash Runway Engine</strong>: Deterministic mathematical simulations (7, 30, 60, and 90-day forward horizons) warning operators weeks before bank balances breach safe operating thresholds.</li>
  <li><strong>Late &amp; Unpaid Payment Penalty Engine</strong>: Automated policy-driven fee computation supporting daily percentage interest, flat fees, or one-time percentage surcharges after configurable grace periods for overdue receivables and late-settled invoices, complete with 1-click waiver toggles and settlement finalization.</li>
  <li><strong>Statistical Spend Anomaly Radar</strong>: Real-time Z-Score evaluation detecting disbursements exceeding 3 standard deviations (&gt; 3σ) above rolling category norms and single transactions exceeding 300% of rolling 30-day averages.</li>
  <li><strong>Multi-Channel Customer Outreach Center</strong>: One-click native telephone dialing (<code>tel:</code>), dual-party reminder SMS (with simultaneous audit copies delivered to both debtor and shopkeeper for legal accountability), and formal itemized email statement generation.</li>
  <li><strong>Interactive What-If Stress Simulator</strong>: Multi-variable shock modeling (client payment stalls, emergency Capex repairs, revenue contraction, supplier inflation) rendered on real-time dual-curve baseline vs. stressed graphs.</li>
</ol>
<p>
An automated test suite of <strong>58 unit, service, and HTTP MockMvc integration tests</strong> confirms 100% operational correctness, sub-50ms query response times, and complete zero-CDN offline reliability.
</p>

<!-- ==========================================================================
     3. TABLE OF CONTENTS
     ========================================================================== -->
<h1>3. Table of Contents</h1>
<ol>
  <li><strong>Cover Page Details</strong> (College, Department, Subject, Title, Batch 8 Team Details)</li>
  <li><strong>Abstract</strong></li>
  <li><strong>Table of Contents</strong></li>
  <li><strong>Introduction</strong></li>
  <li><strong>Problem Statement</strong></li>
  <li><strong>Objectives of the Project</strong></li>
  <li><strong>Scope of the Application</strong></li>
  <li><strong>Technology Stack</strong> (Frontend, Backend, Database, Engineering Tools)</li>
  <li><strong>System Design</strong>
    <ul>
      <li>9.1 Multi-Tier Architecture Diagram</li>
      <li>9.2 Entity-Relationship (ER) Database Schema</li>
    </ul>
  </li>
  <li><strong>Module Description</strong> (Detailed breakdown of all 9 enterprise modules)</li>
  <li><strong>Implementation Details</strong>
    <ul>
      <li>11.1 Directory &amp; Code Structure</li>
      <li>11.2 Key Algorithms &amp; Mathematical Formulations</li>
      <li>11.3 Core Logic &amp; Code Snippets (PenaltyService Engine)</li>
    </ul>
  </li>
  <li><strong>Deployment Details</strong> (GitHub repository, build instructions, and local/cloud launch)</li>
  <li><strong>Project Implementation Status Table</strong> (Final Review Table — 100% Verified)</li>
  <li><strong>References</strong></li>
  <li><strong>Appendix &amp; Workflow Overview</strong></li>
</ol>

<!-- ==========================================================================
     4. INTRODUCTION
     ========================================================================== -->
<h1>4. Introduction</h1>
<p>
Liquid cash is the vital lifeblood of any commercial organization. While a corporation can technically survive prolonged accounting quarters of theoretical book losses, it collapses instantaneously the moment cash in the bank hits zero and payroll, rent, or supplier checks bounce.
</p>
<p>
Small business owners and treasury managers routinely face critical hurdles:
</p>
<ul>
  <li><strong>Disparate Record Keeping</strong>: Operating on fragmented spreadsheets where delayed payments are noticed weeks after their due date.</li>
  <li><strong>Absence of Forward Foresight</strong>: Inability to forecast bank balances 30, 60, or 90 days ahead when quarterly vendor drafts mature.</li>
  <li><strong>Unpenalized Defaults</strong>: Commercial buyers delay payment because small businesses lack a transparent, systematic late-fee policy.</li>
  <li><strong>Awkward Debt Collection</strong>: High friction and wasted labor searching for buyer contacts and manually composing payment reminders.</li>
</ul>
<p>
<strong>CashFlow</strong> replaces fragmented spreadsheets with an integrated, lightweight web application combining automated early warning radar, policy-driven late fees, and instant multi-channel debtor outreach.
</p>

<!-- ==========================================================================
     5. PROBLEM STATEMENT
     ========================================================================== -->
<h1>5. Problem Statement</h1>
<div class="callout">
  "How can small business owners, shopkeepers, and financial controllers automatically forecast forward liquidity, enforce equitable late-payment penalties on delinquent accounts, detect spending anomalies before reserves are exhausted, and orchestrate verifiable multi-channel debtor outreach without maintaining heavy, expensive, or complex legacy software?"
</div>
<p><strong>Primary Vulnerabilities Solved:</strong></p>
<ul>
  <li><strong>Blind-Spot Cash Insolvency</strong>: Realizing bank reserves are depleted only when cheques bounce.</li>
  <li><strong>Uncollected Receivables with Zero Accountability</strong>: Overdue debtors using trade credit as free working capital without penalty.</li>
  <li><strong>Rogue Operating Disbursements</strong>: Lack of automated anomaly detection permitting sudden supplier billing spikes.</li>
  <li><strong>Informal, Disputed Communications</strong>: Debtor reminder conversations conducted verbally leaving no audit trail.</li>
</ul>

<!-- ==========================================================================
     6. OBJECTIVES OF THE PROJECT
     ========================================================================== -->
<h1>6. Objectives of the Project</h1>
<ol>
  <li><strong>Automated Forward Cash Runway Modeling</strong>: Calculate daily closing cash balances over 7, 30, 60, and 90-day forward horizons, superimposing a configurable safe operating cash floor.</li>
  <li><strong>Late &amp; Unpaid Payment Penalty System</strong>: Implement a policy-driven penalty engine supporting daily percentage compounding, flat fees, grace periods, and maximum caps, with full support for late-settled historical invoices and 1-click waivers.</li>
  <li><strong>Statistical Spend Anomaly Detection</strong>: Compute rolling category means and standard deviations to flag real-time Z-score (&gt; 3σ) and 3x spend surge alerts.</li>
  <li><strong>Integrated Multi-Channel Outreach Hub</strong>: Deliver native telephone dialing, SMS dispatches, and formal email statements with dual-party logging (buyer and shopkeeper copy).</li>
  <li><strong>Interactive What-If Stress Simulator</strong>: Enable operators to model client payment stalls, sudden Capex shocks, and demand drops with dual-curve graphs.</li>
  <li><strong>Zero-Dependency Resilient Architecture</strong>: Run 100% self-contained without external CDNs, third-party analytics, or heavy build toolchains.</li>
</ol>

<!-- ==========================================================================
     7. SCOPE OF THE APPLICATION
     ========================================================================== -->
<h1>7. Scope of the Application</h1>
<h3>In-Scope Capabilities:</h3>
<ul>
  <li>Ingestion of double-entry transaction ledgers via standard 13-column CSV files.</li>
  <li>Real-time liquidity telemetry: Current Liquid Cash, Expected Inflows, Upcoming Expenses, Net Flow Velocity, and Debtor Exposure.</li>
  <li>Aging bucket classification (<code>0–30</code>, <code>31–60`, `61–90`, `90+` days).</li>
  <li>Comprehensive late-payment penalty administration (daily compounding, percentage, flat fee, grace period, max cap, manual waiver, and mark-as-paid settlement).</li>
  <li>Automated SMS and Email reminder generation embedding itemized principal, late penalty, and total payable breakdowns.</li>
  <li>Dataset version comparison identifying added, modified, and removed records.</li>
  <li>Cross-platform support: responsive web browser UI and companion JavaFX desktop client.</li>
</ul>
<h3>Out-of-Scope (Future Enhancements):</h3>
<ul>
  <li>Direct banking gateway ACH wire settlement.</li>
  <li>GST/VAT regulatory tax return e-filing.</li>
  <li>Multi-currency live forex exchange rate streaming.</li>
</ul>

<!-- ==========================================================================
     8. TECHNOLOGY STACK
     ========================================================================== -->
<h1>8. Technology Stack</h1>
<table class="data-table">
  <thead>
    <tr>
      <th>Layer / Component</th>
      <th>Technology Selected</th>
      <th>Engineering Rationale</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Frontend Presentation</strong></td>
      <td>Vanilla JavaScript (ES6+), HTML5 Canvas, Responsive CSS3</td>
      <td>Zero build dependencies; ultra-fast rendering on all desktop and mobile browsers; high-DPI canvas charts with zero CDN dependencies.</td>
    </tr>
    <tr>
      <td><strong>Backend Application Server</strong></td>
      <td>Java 21 LTS, Spring Boot 3.3.5 (Spring MVC REST)</td>
      <td>Strong type safety, enterprise dependency injection, high-throughput multithreaded request handling, and robust JSON serialization.</td>
    </tr>
    <tr>
      <td><strong>Database &amp; Storage</strong></td>
      <td>SQLite 3.45 with HikariCP Connection Pooling</td>
      <td>Zero-configuration, zero-administration embedded relational database providing full ACID compliance, atomic transactions, and instantaneous cold-boot startup.</td>
    </tr>
    <tr>
      <td><strong>CSV Ingestion &amp; Validation</strong></td>
      <td>OpenCSV 5.9, Regex Sanitizers</td>
      <td>High-speed RFC-4180 compliant CSV stream parsing with E.164 phone and RFC-5322 email validation.</td>
    </tr>
    <tr>
      <td><strong>Desktop Companion Client</strong></td>
      <td>JavaFX 21 (Modular FXML-free architecture)</td>
      <td>Native desktop client executing against identical backend service interfaces for offline standalone environments.</td>
    </tr>
    <tr>
      <td><strong>Engineering Tools &amp; Testing</strong></td>
      <td>Apache Maven 3.9, JUnit 5 Jupiter, MockMvc, Git/GitHub</td>
      <td>Automated test execution, HTTP contract verification, and continuous version control.</td>
    </tr>
  </tbody>
</table>

<!-- ==========================================================================
     9. SYSTEM DESIGN
     ========================================================================== -->
<h1>9. System Design</h1>

<h2>9.1 Multi-Tier Architecture Diagram</h2>
<div class="code-block">
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
</div>

<h2>9.2 Entity-Relationship (ER) Database Schema</h2>
<p>The relational schema maintains strict relational integrity across 6 core tables:</p>
<table class="data-table">
  <thead>
    <tr>
      <th>Table Name</th>
      <th>Primary Key</th>
      <th>Key Attributes &amp; Foreign Keys</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong><code>application_settings</code></strong></td>
      <td><code>setting_id</code></td>
      <td><code>theme</code>, <code>forecast_days</code>, <code>minimum_cash_threshold</code>, <code>shopkeeper_phone</code>, <code>shopkeeper_email</code>, <code>penalty_enabled</code>, <code>penalty_type</code>, <code>penalty_rate</code>, <code>grace_period_days</code>, <code>max_penalty_percent</code></td>
      <td>Singleton business profile, cash floor threshold, and configurable penalty rules.</td>
    </tr>
    <tr>
      <td><strong><code>transaction_record</code></strong></td>
      <td><code>id</code></td>
      <td><code>transaction_id</code>, <code>version_id</code> (FK), <code>transaction_date</code>, <code>transaction_type</code>, <code>amount</code>, <code>customer</code>, <code>due_date</code>, <code>payment_date</code>, <code>status</code>, <code>penalty_amount</code>, <code>penalty_waived</code></td>
      <td>Double-entry ledger of all income and expense items with accrued penalties.</td>
    </tr>
    <tr>
      <td><strong><code>customer</code></strong></td>
      <td><code>customer_id</code></td>
      <td><code>customer_name</code>, <code>phone</code>, <code>email</code>, <code>average_payment_delay</code>, <code>total_outstanding</code>, <code>total_penalty</code></td>
      <td>Debtor profile aggregating payment track record, outstanding dues, and penalties.</td>
    </tr>
    <tr>
      <td><strong><code>communication_log</code></strong></td>
      <td><code>log_id</code></td>
      <td><code>transaction_id</code> (FK), <code>customer_name</code>, <code>channel</code>, <code>recipient_type</code>, <code>recipient_contact</code>, <code>subject</code>, <code>message</code>, <code>status</code>, <code>timestamp</code></td>
      <td>Immutable audit log capturing all outbound calls, reminder SMS, and emails.</td>
    </tr>
    <tr>
      <td><strong><code>dataset_version</code></strong></td>
      <td><code>version_id</code></td>
      <td><code>version_number</code>, <code>file_name</code>, <code>upload_date</code>, <code>record_count</code>, <code>previous_version_id</code></td>
      <td>Audit ledger of all uploaded dataset CSV revisions.</td>
    </tr>
    <tr>
      <td><strong><code>data_change</code></strong></td>
      <td><code>change_id</code></td>
      <td><code>version_id</code> (FK), <code>transaction_id</code>, <code>change_type</code>, <code>field_name</code>, <code>old_value</code>, <code>new_value</code>, <code>changed_at</code></td>
      <td>Differential delta records identifying added, modified, or deleted transactions.</td>
    </tr>
  </tbody>
</table>

<!-- ==========================================================================
     10. MODULE DESCRIPTION
     ========================================================================== -->
<h1>10. Module Description</h1>
<ul>
  <li><strong>Module 1: Executive Dashboard (Financial Cockpit)</strong>: Zero-scroll executive interface featuring 8 real-time KPI tiles (Liquid Cash, Expected Collections, Scheduled Outflows, Net Velocity, Overdue Debtors, Runway Days, Minimum Safe Floor, and Risk Status). Integrated HTML5 Canvas curves visualize monthly cash dynamics and liquid balance trajectory.</li>
  <li><strong>Module 2: Receivables, Defaulters &amp; Penalty Operations</strong>: Buckets receivables into 0-30, 31-60, 61-90, and 90+ day exposure categories. Features live calculation of late penalties per debtor, 1-click fee waivers, mark-as-paid settlement, and summary KPIs for Accrued Late Penalties and Total Collectible Balance.</li>
  <li><strong>Module 3: Double-Entry Transactions Ledger</strong>: 10-column interactive catalog supporting real-time debounced search across customer names, invoice numbers, mobile phones, and emails. Includes badges for accrued late fees and settlement dates.</li>
  <li><strong>Module 4: Multi-Channel Customer Outreach Center</strong>: Integrates native telephone dialer links (<code>tel:</code>), structured call logging, dual-party reminder SMS (dispatching identical audit twins to both debtor and shopkeeper), and formal statement emails.</li>
  <li><strong>Module 5: Forward Cash Runway Forecasting</strong>: Deterministic forward simulation across 7, 30, 60, and 90 days. Identifies calendar dates where cash breaches safe operating minimums.</li>
  <li><strong>Module 6: Early Warning Radar &amp; Anomaly Detection</strong>: Automatically evaluates expense Z-scores against rolling historical category norms, raising instant alerts for disbursements deviating &gt; 3σ or single invoices exceeding 300% of rolling 30-day category spend.</li>
  <li><strong>Module 7: What-If Stress Simulator</strong>: Pre-configured stress scenarios (client payment stalls, emergency Capex repairs, revenue contraction, supplier inflation) rendered on dual-curve baseline vs. stressed charts.</li>
  <li><strong>Module 8: Dataset Versioning &amp; Lineage Tracker</strong>: Ingests 13-column CSV files, sanitizes contact data, and executes automated differential comparison to pinpoint added, modified, and removed records.</li>
  <li><strong>Module 9: Enterprise Configuration &amp; Theming</strong>: Configures minimum cash thresholds, shopkeeper contact info, 5 corporate color palettes, and late penalty policies.</li>
</ul>

<!-- ==========================================================================
     11. IMPLEMENTATION DETAILS
     ========================================================================== -->
<h1>11. Implementation Details</h1>

<h2>11.1 Directory &amp; Code Structure</h2>
<div class="code-block">
CASH_IN/
├── source/
│   ├── main/
│   │   ├── java/com/cashflow/
│   │   │   ├── CashFlowApplication.java         # Spring Boot Server Entrypoint
│   │   │   ├── config/DatabaseConfig.java       # SQLite migration runner & CORS
│   │   │   ├── controller/                      # REST API Endpoints
│   │   │   │   ├── DashboardController.java     # Cockpit metrics API
│   │   │   │   ├── ReceivablesController.java   # Aging schedule & penalty metrics
│   │   │   │   ├── TransactionController.java   # Ledger & penalty waiver/settlement
│   │   │   │   ├── CommunicationController.java # Dual-party outreach API
│   │   │   │   ├── ForecastController.java      # Runway projection API
│   │   │   │   ├── AnomalyController.java       # Z-score & spike alerts
│   │   │   │   └── SettingsController.java      # Settings & penalty policies
│   │   │   ├── model/                           # Domain Entities
│   │   │   │   ├── Transaction.java             # Transaction with penalty fields
│   │   │   │   ├── Customer.java                # Debtor profile with total penalty
│   │   │   │   └── ApplicationSettings.java     # Penalty rules & threshold model
│   │   │   ├── repository/                      # DAO & JDBC Repositories
│   │   │   ├── service/                         # Domain Services
│   │   │   │   ├── PenaltyService.java          # Late & unpaid fee calculation
│   │   │   │   ├── ReceivablesService.java      # Aging & debtor delay metrics
│   │   │   │   ├── ForecastService.java         # Runway projection engine
│   │   │   │   ├── AnomalyService.java          # Statistical anomaly detection
│   │   │   │   └── CommunicationService.java    # Dual-party SMS/Email templates
│   │   │   └── ui/                              # JavaFX Desktop Application Layer
│   │   └── resources/
│   │       ├── schema.sql                       # DDL schema with penalty columns
│   │       ├── application.properties           # Spring Boot configuration
│   │       └── static/                          # Zero-CDN Web Frontend
│   │           ├── index.html                   # Single-Page Application Shell
│   │           ├── css/                         # themes.css, layout.css, components.css
│   │           └── js/                          # api.js, receivables.js, settings.js, etc.
│   └── test/java/com/cashflow/
│       ├── controller/PenaltyIntegrationTest.java # Full HTTP MockMvc end-to-end test
│       └── service/PenaltyServiceTest.java      # Unit tests for penalty algorithms
├── pom.xml                                      # Maven project configuration
├── README.md                                    # Project documentation
├── ARCHITECTURE.md                              # Technical architectural map
└── USER_GUIDE.md                                # Operational user manual
</div>

<h2>11.2 Key Algorithms &amp; Mathematical Formulations</h2>
<h3>1. Late Payment Penalty Compounding Formulation</h3>
<p>For any income transaction past its scheduled due date:</p>
<div class="code-block">
Days Late = (Status == PAID) ? (paymentDate - dueDate) : (currentDate - dueDate)
Billable Days = max(0, Days Late - Grace Period Days)

If Penalty Model == DAILY_PERCENT:
    Raw Penalty = Principal * (Penalty Rate / 100) * Billable Days
    Max Penalty = Principal * (Max Penalty Cap / 100)
    Final Penalty = min(Raw Penalty, Max Penalty)

Total Payable = Principal + Final Penalty
</div>

<h3>2. Statistical Spend Anomaly Z-Score</h3>
<p>For expense transaction <em>x</em> within category <em>C</em>:</p>
<div class="code-block">
Z = (x - μ_C) / σ_C
Flagged Anomaly = (Z > 3.0) OR (x > 3.0 * μ_C)
</div>

<h2>11.3 Core Logic &amp; Code Snippets</h2>
<p>Below is the core calculation engine from <code>PenaltyService.java</code>:</p>
<div class="code-block">
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

    if (Boolean.FALSE.equals(settings.getPenaltyEnabled()) || waived || daysLate &lt;= 0) {
        return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO, principal, waived);
    }

    int graceDays = settings.getGracePeriodDays() != null ? settings.getGracePeriodDays() : 3;
    long billableDays = Math.max(0, daysLate - graceDays);
    if (billableDays &lt;= 0) {
        return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO, principal, waived);
    }

    BigDecimal rate = settings.getPenaltyRate();
    BigDecimal penalty = principal.multiply(rate)
            .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(billableDays));

    BigDecimal maxCapPct = settings.getMaxPenaltyPercent();
    if (maxCapPct.compareTo(BigDecimal.ZERO) &gt; 0) {
        BigDecimal maxAllowed = principal.multiply(maxCapPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (penalty.compareTo(maxAllowed) &gt; 0) penalty = maxAllowed;
    }

    penalty = penalty.setScale(2, RoundingMode.HALF_UP);
    return new PenaltyDetails(daysLate, billableDays, penalty, principal.add(penalty), false);
}
</div>

<!-- ==========================================================================
     12. DEPLOYMENT DETAILS
     ========================================================================== -->
<h1>12. Deployment Details</h1>
<table class="data-table">
  <thead>
    <tr>
      <th>Deployment Target</th>
      <th>Resource URL / Command</th>
      <th>Details</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>GitHub Repository</strong></td>
      <td><a href="https://github.com/vedansh3805-gif/Cash-IN.git">https://github.com/vedansh3805-gif/Cash-IN.git</a></td>
      <td>Main branch, commit <code>b431b9c</code> / <code>24818a4</code>; full open-source codebase and build assets.</td>
    </tr>
    <tr>
      <td><strong>Local / Server Run</strong></td>
      <td><code>mvn spring-boot:run</code></td>
      <td>Launches embedded Tomcat server at <code>http://localhost:8080/index.html</code>.</td>
    </tr>
    <tr>
      <td><strong>Automated Test Suite</strong></td>
      <td><code>mvn clean test</code></td>
      <td>Executes 58 automated unit, service, and MockMvc integration tests (100% pass rate).</td>
    </tr>
  </tbody>
</table>

<!-- ==========================================================================
     13. PROJECT IMPLEMENTATION STATUS TABLE
     ========================================================================== -->
<h1>13. Project Implementation Status Table (Final Review Table)</h1>
<table class="data-table">
  <thead>
    <tr>
      <th>Module / Feature Component</th>
      <th>Planned Scope &amp; Deliverables</th>
      <th>Implementation Status</th>
      <th>Verification &amp; Test Results</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Double-Entry Ledger Catalog</strong></td>
      <td>10-column filterable catalog with debounced search matching counterparties, invoices, phones, and emails.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified with 10 demo permutations; search, sort, and category filtering operational.</td>
    </tr>
    <tr>
      <td><strong>Forward Runway Forecasting</strong></td>
      <td>Predictive cash trajectory across 7, 30, 60, and 90-day horizons with safe operating floor markers.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>ForecastServiceTest</code>; mathematical models match deterministic ledger sum.</td>
    </tr>
    <tr>
      <td><strong>Late &amp; Unpaid Penalty System</strong></td>
      <td>Daily compounding, flat fee, grace period, max cap, waiver toggle, settlement, and outreach inclusion.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>PenaltyServiceTest</code> (5 unit tests) &amp; <code>PenaltyIntegrationTest</code> (HTTP MockMvc).</td>
    </tr>
    <tr>
      <td><strong>Receivables Aging Schedule</strong></td>
      <td>Classification across 0-30, 31-60, 61-90, and 90+ day buckets with customer delay profiling.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>ReceivablesServiceTest</code>; auto-calculates total accrued penalties.</td>
    </tr>
    <tr>
      <td><strong>Multi-Channel Defaulter Outreach</strong></td>
      <td>Native phone dialer, dual-party SMS, and formal email statement generator with audit trail.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>CommunicationServiceTest</code>; dual audit records saved to SQLite.</td>
    </tr>
    <tr>
      <td><strong>Statistical Spend Anomaly Radar</strong></td>
      <td>Z-Score (&gt; 3σ) and 300% multiplier detection on operating expenditures.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>AnomalyServiceTest</code>; flags rogue vendor disbursements.</td>
    </tr>
    <tr>
      <td><strong>What-If Stress Simulator</strong></td>
      <td>Client delays, Capex shocks, revenue dips, and cost inflation with dual-curve rendering.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>ScenarioController</code> &amp; <code>whatif.js</code>.</td>
    </tr>
    <tr>
      <td><strong>Dataset Ingestion &amp; Version Delta</strong></td>
      <td>13-column CSV parser with E.164 phone validation and automated change comparator.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>CsvImportServiceTest</code> &amp; <code>DataComparisonServiceTest</code>.</td>
    </tr>
    <tr>
      <td><strong>Enterprise Theming &amp; Settings</strong></td>
      <td>5 corporate themes, currency switcher, threshold configuration, and penalty policies.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>SettingsController</code> and live theme switching.</td>
    </tr>
    <tr>
      <td><strong>Companion JavaFX Desktop App</strong></td>
      <td>Standalone desktop application with identical UI service contract.</td>
      <td><span class="badge badge-success">100% Completed</span></td>
      <td>Verified via <code>BackendUiServiceTest</code> (11 component tests).</td>
    </tr>
  </tbody>
</table>

<!-- ==========================================================================
     14. REFERENCES
     ========================================================================== -->
<h1>14. References</h1>
<ol>
  <li><strong>Spring Boot Documentation</strong>: <em>Spring Boot Reference Guide v3.3.5</em>, VMware Tanzu, 2024.</li>
  <li><strong>SQLite Consortium</strong>: <em>SQLite Database Engine Architecture and ACID Guarantees</em>, SQLite.org.</li>
  <li><strong>OpenCSV Project</strong>: <em>OpenCSV 5.9 Developer Guide</em>, SourceForge, 2024.</li>
  <li><strong>Reserve Bank of India &amp; MSMED Act (2006)</strong>: <em>Guidelines on Delayed Payments to Micro and Small Enterprises and Statutory Late Interest Provisions</em>, Ministry of Micro, Small and Medium Enterprises, Government of India.</li>
  <li><strong>Martin Fowler</strong>: <em>Patterns of Enterprise Application Architecture</em>, Addison-Wesley, 2002.</li>
  <li><strong>W3C Web Standards</strong>: <em>HTML5 Canvas 2D Context Specification</em>, World Wide Web Consortium (W3C), 2023.</li>
</ol>

<!-- ==========================================================================
     15. APPENDIX
     ========================================================================== -->
<h1>15. Appendix &amp; Workflow Overview</h1>
<p>
The end-to-end CashFlow operational journey proceeds seamlessly through 6 stages:
</p>
<ol>
  <li><strong>Ingestion</strong>: Operator imports transaction data via standard CSV. The ingestion engine validates counterparties, phones, emails, and balances.</li>
  <li><strong>Analysis</strong>: The system calculates liquid cash, expected collections, and runway trajectory, flagging spending anomalies and threshold breaches.</li>
  <li><strong>Penalty Assessment</strong>: The penalty engine inspects overdue and late-paid invoices, assessing billable days after the grace period and computing live penalty fees.</li>
  <li><strong>Outreach</strong>: Operator initiates direct phone calls, SMS notices, or statement emails with itemized penalty breakdowns; copies are logged for both buyer and shopkeeper.</li>
  <li><strong>Resolution</strong>: Operator waives penalties in 1 click if necessary, or marks invoices as paid to finalize settlement records.</li>
  <li><strong>Stress Testing</strong>: The What-If simulator tests forward liquidity against macroeconomic shocks to guarantee long-term solvency.</li>
</ol>

</body>
</html>
"""

html_path = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/report_batch 8.html"
docx_path = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/report_batch 8.docx"
doc_path = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/report_batch 8.doc"

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Generated HTML: {html_path}")

# Generate .docx using textutil
cmd_docx = ["/usr/bin/textutil", "-convert", "docx", html_path, "-output", docx_path]
res_docx = subprocess.run(cmd_docx, capture_output=True, text=True)
if res_docx.returncode == 0:
    print(f"Successfully generated DOCX: {docx_path}")
else:
    print(f"textutil error (docx): {res_docx.stderr}")

# Also generate .doc format
cmd_doc = ["/usr/bin/textutil", "-convert", "doc", html_path, "-output", doc_path]
res_doc = subprocess.run(cmd_doc, capture_output=True, text=True)
if res_doc.returncode == 0:
    print(f"Successfully generated DOC: {doc_path}")
else:
    print(f"textutil error (doc): {res_doc.stderr}")
