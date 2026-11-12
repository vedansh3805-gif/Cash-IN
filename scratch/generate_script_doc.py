import os
import subprocess

html_content = """<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>CashFlow ERP — Comprehensive Presentation Script for 4 Members</title>
<style>
  body {
    font-family: Calibri, 'Segoe UI', Arial, sans-serif;
    line-height: 1.6;
    color: #1e293b;
    padding: 30px;
    max-width: 900px;
    margin: 0 auto;
  }
  h1 {
    color: #0f172a;
    font-size: 26pt;
    border-bottom: 2pt solid #2563eb;
    padding-bottom: 8pt;
    margin-bottom: 4pt;
  }
  .subtitle {
    color: #2563eb;
    font-size: 14pt;
    font-weight: bold;
    margin-bottom: 15pt;
  }
  .meta-box {
    background-color: #f1f5f9;
    border-left: 4pt solid #0ea5e9;
    padding: 10pt;
    margin-bottom: 20pt;
    font-size: 10pt;
  }
  h2 {
    color: #1e3a8a;
    font-size: 16pt;
    border-bottom: 1pt solid #cbd5e1;
    padding-bottom: 4pt;
    margin-top: 24pt;
  }
  h3 {
    color: #0f172a;
    font-size: 13pt;
    margin-top: 14pt;
  }
  .speaker-card {
    background-color: #f8fafc;
    border: 1pt solid #cbd5e1;
    border-left: 5pt solid #2563eb;
    padding: 12pt;
    margin: 14pt 0;
  }
  .speaker-title {
    font-size: 12pt;
    font-weight: bold;
    color: #1e3a8a;
    text-transform: uppercase;
    letter-spacing: 0.5pt;
    margin-bottom: 6pt;
  }
  .slide-tag {
    display: inline-block;
    background-color: #dbeafe;
    color: #1d4ed8;
    font-weight: bold;
    padding: 2pt 8pt;
    border-radius: 4pt;
    font-size: 9pt;
    margin-bottom: 8pt;
  }
  .action-cue {
    background-color: #fef3c7;
    border-left: 3pt solid #f59e0b;
    color: #92400e;
    padding: 6pt 10pt;
    font-size: 10pt;
    font-style: italic;
    margin: 8pt 0;
  }
  .speech {
    font-size: 11pt;
    color: #0f172a;
    line-height: 1.55;
    margin-bottom: 8pt;
  }
  .handoff {
    background-color: #ecfdf5;
    border-left: 3pt solid #10b981;
    color: #065f46;
    padding: 6pt 10pt;
    font-size: 10pt;
    font-weight: bold;
    margin-top: 10pt;
  }
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 14pt 0;
    font-size: 10pt;
  }
  th {
    background-color: #1e3a8a;
    color: white;
    text-align: left;
    padding: 8pt;
    border: 1pt solid #1e3a8a;
  }
  td {
    padding: 7pt 8pt;
    border: 1pt solid #cbd5e1;
  }
  tr:nth-child(even) {
    background-color: #f8fafc;
  }
  .qa-box {
    background-color: #f8fafc;
    border: 1pt solid #e2e8f0;
    padding: 10pt;
    margin-bottom: 12pt;
    border-radius: 4pt;
  }
  .qa-q {
    font-weight: bold;
    color: #b91c1c;
    font-size: 11pt;
  }
  .qa-a {
    color: #0f172a;
    font-size: 10.5pt;
    margin-top: 4pt;
  }
</style>
</head>
<body>

<h1>CASHFLOW ERP (CASH-IN)</h1>
<div class="subtitle">Complete Review-2 Presentation Master Script for 4 Team Members</div>

<div class="meta-box">
  <strong>Project:</strong> CashFlow — Small Business Liquidity Management & Cash Runway Early Warning System<br>
  <strong>Presentation Duration:</strong> 10 – 12 Minutes Total (~2.5 to 3 Minutes per Speaker) + 3 Minutes Q&A<br>
  <strong>Evaluation Stage:</strong> Review - 2 Milestone (Current Status: 85%–90% Realistic Completion)<br>
  <strong>Team Distribution:</strong> 4 Members (Architecture Lead, Backend Engineer, UI/UX Lead, QA/Analytics Lead)<br>
  <strong>Slide Deck Reference:</strong> CashFlow_Review2_Presentation.pptx (9 Slides Total)
</div>

<h2>1. TEAM ROLE DISTRIBUTION & TIMING BREAKDOWN</h2>

<table>
  <thead>
    <tr>
      <th>Member</th>
      <th>Designated Role</th>
      <th>Assigned Slides</th>
      <th>Core Focus Areas</th>
      <th>Time</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Member 1</strong></td>
      <td>Lead Architect &amp; System Intro</td>
      <td>Slide 1 &amp; Slide 2</td>
      <td>Project vision, SME liquidity problem, 85%–90% project health, Review-2 checklist</td>
      <td>2.5 Mins</td>
    </tr>
    <tr>
      <td><strong>Member 2</strong></td>
      <td>Backend &amp; Service Specialist</td>
      <td>Slide 3 &amp; Slide 5</td>
      <td>System architecture, decoupled service boundary (ADR 2), dual-party outreach engine</td>
      <td>2.5 Mins</td>
    </tr>
    <tr>
      <td><strong>Member 3</strong></td>
      <td>Frontend &amp; UI/UX Specialist</td>
      <td>Slide 4 &amp; Slide 6</td>
      <td>Review-2 Component Triad, custom Canvas engine, live dashboard &amp; forecast runway</td>
      <td>3.0 Mins</td>
    </tr>
    <tr>
      <td><strong>Member 4</strong></td>
      <td>Analytics, QA &amp; Roadmap Lead</td>
      <td>Slide 7, Slide 8 &amp; Slide 9</td>
      <td>What-If Stress Simulator, 52/52 automated tests, architectural invariants, future roadmap</td>
      <td>3.0 Mins</td>
    </tr>
  </tbody>
</table>

<hr>

<h2>2. WORD-FOR-WORD PRESENTATION SCRIPT</h2>

<!-- ========================================================================= -->
<!-- MEMBER 1 -->
<!-- ========================================================================= -->
<div class="speaker-card">
  <div class="speaker-title">MEMBER 1 — LEAD ARCHITECT &amp; SYSTEM OVERVIEW</div>
  <div class="slide-tag">COVERING SLIDES: SLIDE 1 &amp; SLIDE 2</div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Display Slide 1 (Title Slide: CashFlow ERP - Early Warning System). Maintain confident eye contact with evaluators.
  </div>

  <div class="speech">
    "Respected evaluators and faculty members, good morning/afternoon. Today, my team and I are proud to present our capstone project: <strong>CashFlow ERP</strong>, an enterprise-grade liquidity management and predictive cash runway early warning system designed specifically for small businesses, retail distributors, and suppliers.
  </div>
  <div class="speech">
    Our team comprises four members: myself, <strong>Vedansh Saxena</strong>, acting as the Lead Architect; <strong>[Member 2 Name]</strong>, leading our backend and persistence tier; <strong>[Member 3 Name]</strong>, our frontend and data visualization engineer; and <strong>[Member 4 Name]</strong>, heading scenario analytics and quality assurance.
  </div>
  <div class="speech">
    To understand the foundation of our project, consider a stark economic reality: <strong>over 82% of small businesses fail not because of a lack of sales or accounting profitability, but because of sudden, unmanaged cash flow insolvency</strong>. In standard business operations, goods are sold on 30-to-60 day credit terms. While invoices sit pending, immediate liabilities—such as monthly payroll, office rent, utility bills, and vendor raw materials—demand instant cash outflows.
  </div>
  <div class="speech">
    Existing commercial accounting suites like Tally, QuickBooks, or SAP excel at retrospective bookkeeping and tax compliance. However, they lack <strong>proactive forward forecasting</strong>, automated debtor outreach, and real-time anomaly radars. CashFlow bridges this vital operational gap.
  </div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Transition to Slide 2 (Project Implementation Status &amp; Milestone Verification Table).
  </div>

  <div class="speech">
    "Moving to Slide 2, we present our <strong>Review-2 Implementation Status</strong>. While the academic requirement for Review 2 was a 50% milestone, we are pleased to report that our engineering execution has vastly exceeded this benchmark—our platform currently stands at <strong>85% to 90% realistic production completion</strong>.
  </div>
  <div class="speech">
    Let us review our milestone deliverables:
    <ul>
      <li><strong>Module 1 — CSV Ingestion &amp; Schema Sanitization:</strong> 100% Complete. We stream-parse standard 13-column financial schemas using OpenCSV 5.9, validating E.164 phone numbers and emails without destructive database overwrites. Tested across three benchmark operational datasets.</li>
      <li><strong>Module 2 — Differential Versioning Engine:</strong> 100% Complete. Tracks transaction-keyed changes across versions, automatically categorizing delta records as ADDED, MODIFIED, REMOVED, or UNCHANGED.</li>
      <li><strong>Module 3 — Forward Forecasting &amp; Early Warning Radar:</strong> 100% Complete. Proactively projects liquid cash curves over 7, 30, 60, and 90-day horizons, monitoring against our dynamic safety floor of ₹50,000.</li>
      <li><strong>Module 4 — Multi-Channel Debtor Outreach:</strong> 100% Complete. Enables instant telephone calling, dual-party SMS reminders, and formal statement emails with immutable audit logging.</li>
      <li><strong>Module 5 — Decoupled UI Architecture:</strong> 100% Complete, ensuring view portability across web and desktop.</li>
      <li><strong>Module 6 — REST Web Services:</strong> 90% Complete, spanning 13 Spring Boot controllers and over 32 endpoints with sub-50 millisecond query latency.</li>
      <li><strong>Module 7 — What-If Stress Simulator:</strong> 92% Complete, already providing 4 preset shock scenarios and dynamic multi-adjustment modeling.</li>
    </ul>
  </div>

  <div class="handoff">
    [HANDOFF]: "To explain how these capabilities are structured in our system design and backend architecture, I now hand over the presentation to Member 2."
  </div>
</div>

<!-- ========================================================================= -->
<!-- MEMBER 2 -->
<!-- ========================================================================= -->
<div class="speaker-card">
  <div class="speaker-title">MEMBER 2 — BACKEND &amp; SERVICE SPECIALIST</div>
  <div class="slide-tag">COVERING SLIDES: SLIDE 3 &amp; SLIDE 5</div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Display Slide 3 (System Design &amp; Decoupled Architectural Patterns).
  </div>

  <div class="speech">
    "Thank you, [Member 1]. Respected committee, I will now walk you through the system design, decoupling patterns, and specialized core capabilities that power CashFlow.
  </div>
  <div class="speech">
    As detailed on Slide 3, our system follows a high-performance, three-tier architecture engineered around two critical architectural decision records—<strong>ADR 1 and ADR 2</strong>:
  </div>
  <div class="speech">
    <strong>1. Dual Presentation Tier:</strong> CashFlow provides two synchronized presentation clients: a full-featured, zero-CDN web single-page application, and an offline companion desktop GUI built in JavaFX 21. Both interfaces share identical responsive palettes and design tokens.
  </div>
  <div class="speech">
    <strong>2. The Decoupled Service Boundary (ADR 2):</strong> A key highlight of our engineering is the <code>CashFlowUiService</code> interface. In enterprise software, UI development is frequently blocked by database schemas or network environments. By defining a clean Java interface, our presentation layer is completely isolated from data persistence. We implemented two providers:
    <ul>
      <li><code>BackendUiService</code>: Connects to live Spring Boot 3 services and SQLite database for real operations.</li>
      <li><code>MockUiService</code>: A zero-database, in-memory synthetic provider that allows our JavaFX companion desktop application to launch in under 2 seconds without requiring an active database or network connection.</li>
    </ul>
    This guarantees zero view coupling—our UI views do not import a single SQL or network library.
  </div>
  <div class="speech">
    <strong>3. Persistence &amp; Business Engine Tier:</strong> In the backend, we run <strong>Spring Boot 3.3.5 on Java 21</strong>, utilizing an embedded <strong>SQLite 3.45</strong> database managed through a high-performance <strong>HikariCP connection pool</strong>. SQLite provides ACID-compliant transactions in a zero-configuration single file (<code>cashflow.db</code>), ensuring sub-millisecond local reads and zero database server overhead.
  </div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Transition to Slide 5 (Specialized Capability: Multi-Channel Debtor Outreach &amp; Dual-Party Logging).
  </div>

  <div class="speech">
    "Moving to Slide 5, let us examine our most innovative operational feature: <strong>Multi-Channel Debtor Outreach with Dual-Party Synchronization (ADR 5 &amp; 6)</strong>.
  </div>
  <div class="speech">
    In small businesses, collecting overdue receivables is often uncomfortable and poorly documented. CashFlow eliminates this friction with a unified communications desk offering three distinct channels:
    <ol>
      <li><strong>Native Telephony Dialer:</strong> Utilizing the operating system's <code>tel:</code> protocol, the operator can click to dial any debtor directly. The modal displays overdue invoice amounts, late days, and logs telephone notes with promised settlement dates.</li>
      <li><strong>Dual-Party SMS Reminders:</strong> Formats an SMS containing the specific invoice ID, due date, and bank remittance instructions, complete with a live 160-character budget counter.</li>
      <li><strong>Formal Statement Email &amp; Bulk Dispatch:</strong> Generates itemized billing statements with payment terms and penalties, with a 1-click bulk broadcast option that notifies all overdue accounts simultaneously.</li>
    </ol>
  </div>
  <div class="speech">
    <strong>The Core Innovation — Dual-Party Synchronization:</strong> Whenever any collection notification is dispatched, CashFlow dispatches notifications simultaneously to <em>both parties</em>. The buyer receives the payment alert, and the shopkeeper instantly receives an exact audit confirmation copy. Both entries are permanently recorded in our SQLite <code>communication_log</code> table, ensuring complete transparency and dispute-free bookkeeping.
  </div>

  <div class="handoff">
    [HANDOFF]: "Now, to demonstrate how these backend engines translate into our user interface and live analytical dashboards, I hand over to Member 3."
  </div>
</div>

<!-- ========================================================================= -->
<!-- MEMBER 3 -->
<!-- ========================================================================= -->
<div class="speaker-card">
  <div class="speaker-title">MEMBER 3 — FRONTEND &amp; UI/UX SPECIALIST</div>
  <div class="slide-tag">COVERING SLIDES: SLIDE 4 &amp; SLIDE 6</div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Display Slide 4 (Frontend Modules Hierarchy &amp; Academic Rubric Alignment).
  </div>

  <div class="speech">
    "Thank you, [Member 2]. Good day, evaluators. I will now present the frontend module hierarchy, our academic rubric compliance, and the live operational metrics of CashFlow.
  </div>
  <div class="speech">
    On Slide 4, you can see how our presentation layer is structured directly around the <strong>Review-2 Academic Evaluation Rubric</strong> via our <strong>Core Component Triad</strong>:
    <ul>
      <li><strong>Component 1 — Home Page (Executive Dashboard):</strong> Features an enterprise brand header, dynamic breadcrumbs, session status indicators, an 8-card responsive KPI grid, and three zero-dependency Canvas charts engineered with responsive auto-fit CSS grids.</li>
      <li><strong>Component 2 — Menu &amp; Catalog Page (Transactions &amp; Receivables):</strong> Houses our itemized 10-column financial ledger. Includes 300ms debounced real-time search across counterparties, phone numbers, and categories, combined with multi-select flow filters, aging schedule breakdowns (0-30, 31-60, 61-90, 90+ days), and clickable contact action pills.</li>
      <li><strong>Component 3 — Login &amp; Authentication Page:</strong> A responsive modal overlay with input sanitization, non-blocking toast notifications, and 1-click evaluation personas: <em>Admin</em>, <em>Auditor</em>, and <em>Shopkeeper</em>, accompanied by a seamless topbar logout flow.</li>
    </ul>
  </div>
  <div class="speech">
    <strong>Zero-CDN Canvas Engine (ADR 1):</strong> Instead of relying on heavy third-party CDNs like Chart.js which can fail in offline business environments, we built <code>EnterpriseChart</code> in <code>charts.js</code>—a custom, 2D HTML5 Canvas engine under 30KB. It renders at 60 frames per second, supports high-DPI Retina scaling, and automatically redraws across all <strong>5 built-in enterprise themes</strong>: <em>Arctic Ledger, Forest Ledger, Copper Desk, Ocean Glass, and Paper &amp; Ink</em>.
  </div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Transition to Slide 6 (UI/UX Live Demo: Executive Cockpit &amp; Predictive Runway Forecast).
  </div>

  <div class="speech">
    "On Slide 6, we demonstrate the system operating on our baseline accounting dataset (25 transactions):
  </div>
  <div class="speech">
    <strong>The Executive Financial Cockpit:</strong>
    Notice the top KPI strip:
    <ul>
      <li>Our <strong>Liquid In-Bank Cash</strong> stands at ₹1,42,850.</li>
      <li>We have <strong>Expected Inflows</strong> of ₹5,80,000 against <strong>Scheduled Outflows</strong> of ₹4,37,150.</li>
      <li>This yields a positive <strong>Net Period Velocity</strong> of +₹1,42,850.</li>
      <li>However, our <strong>Overdue Defaulter Exposure</strong> alerts the controller to ₹75,000 in delayed payments from <em>PQR Industries</em>.</li>
      <li>Our <strong>Safe Operating Cash Floor</strong> is configured at ₹50,000, and the overall liquidity health badge reports <strong>SAFE</strong>.</li>
    </ul>
  </div>
  <div class="speech">
    <strong>Predictive Forward Cash Forecast:</strong>
    Directly below, our forward runway chart projects closing cash balances across 7, 30, 60, and 90-day horizons. Notice the <strong>red dashed line at ₹50,000</strong> representing our minimum liquidity threshold. CashFlow dynamically factors in pending customer invoices, scheduled vendor bills, and recurring commitments like payroll. If collections are delayed, our <strong>Early Warning Radar</strong> triggers a predictive breach warning—such as a projected cash shortfall on Day 25—giving management weeks to act rather than reacting after a payroll check bounces.
  </div>

  <div class="handoff">
    [HANDOFF]: "Now, to demonstrate our What-If Stress Simulator, automated test verification, and future roadmap, I hand over to Member 4."
  </div>
</div>

<!-- ========================================================================= -->
<!-- MEMBER 4 -->
<!-- ========================================================================= -->
<div class="speaker-card">
  <div class="speaker-title">MEMBER 4 — ANALYTICS, QA &amp; ROADMAP LEAD</div>
  <div class="slide-tag">COVERING SLIDES: SLIDE 7, SLIDE 8 &amp; SLIDE 9</div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Display Slide 7 (UI/UX Live Demo: Debtor Desk &amp; What-If Stress Simulator).
  </div>

  <div class="speech">
    "Thank you, [Member 3]. Good day, evaluators. I will conclude our presentation by demonstrating our What-If scenario modeling, our test audit metrics, and our future roadmap.
  </div>
  <div class="speech">
    On Slide 7, we present the <strong>What-If Stress Simulator</strong>. Business owners frequently ask: <em>'What happens to our payroll next month if our largest client delays payment by 3 weeks, or if an emergency machinery breakdown occurs?'</em>
  </div>
  <div class="speech">
    CashFlow answers this without risking live database records:
    <ul>
      <li><strong>4 One-Click Preset Shock Chips:</strong> We built instant scenario presets: (1) <em>30-Day Client Delay</em>, (2) <em>Emergency Capex Shock</em> (₹1,00,000 machine repair), (3) <em>25% Demand Dip</em>, and (4) <em>15% Cost Inflation</em>.</li>
      <li><strong>Dynamic Multi-Adjustment Builder:</strong> Operators can combine multiple custom shocks simultaneously. If an adjustment date is omitted, our backend automatically activates an intelligent Day-1 fallback.</li>
      <li><strong>Dual-Trajectory Canvas Chart:</strong> The canvas renders the baseline runway curve alongside the stressed simulation curve in real time, calculating the net deficit and new minimum cash floor instantly. Most importantly, the simulation executes against in-memory clones—zero persistent accounting data is altered.</li>
    </ul>
  </div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Transition to Slide 8 (Test Execution Audit Report: 100% Passing Verification).
  </div>

  <div class="speech">
    "Moving to Slide 8, software reliability is paramount in financial systems. We verified our platform through an exhaustive automated test suite executed via Apache Maven and JUnit 5:
    <ul>
      <li><strong>52 out of 52 Automated Tests Passed (100% Pass Rate with 0 Failures, 0 Errors, 0 Skipped).</strong></li>
      <li>Across these suites, over <strong>130 individual verification assertions</strong> were confirmed.</li>
      <li><code>ComprehensiveDryRunTest</code> executes 17 comprehensive integration scenarios spanning dataset ingestion, KPI aggregation, multi-channel outreach, forecasting, and CSV export.</li>
      <li><code>PermutationCombinationTest</code> verifies 5 complex test matrices covering all dataset versions, compound multi-shocks, blank date handling, and theme switches.</li>
    </ul>
    Crucially, these tests mathematically prove four <strong>Architectural Invariants</strong>: (1) Zero-CDN dependency, (2) Data Immutability during simulations, (3) Safe non-destructive SQLite schema migration on launch, and (4) Dual-Party logging compliance on every outreach trigger.
  </div>

  <div class="action-cue">
    [ACTION ON SCREEN]: Transition to Slide 9 (Conclusion &amp; Future Roadmap).
  </div>

  <div class="speech">
    "To conclude on Slide 9: CashFlow has successfully completed and exceeded all Review-2 requirements, achieving a realistic completion rate of <strong>85% to 90%</strong>.
  </div>
  <div class="speech">
    For our final review and production release, our roadmap includes:
    <ol>
      <li><strong>Live Payment Gateway Webhooks:</strong> Integrating Razorpay and Stripe to embed dynamic payment links inside reminder SMS/emails, enabling automated reconciliation when clients pay.</li>
      <li><strong>External Carrier Telephony:</strong> Adding Twilio / AWS SNS credentials for automated live cellular dispatches with delivery receipt webhooks.</li>
      <li><strong>Open Banking Feeds:</strong> Connecting to Account Aggregator APIs for direct, automated bank statement streaming.</li>
    </ol>
  </div>
  <div class="speech">
    Thank you for your time and attention. My team and I are now open to any questions."
  </div>
</div>

<hr>

<h2>3. VIVA &amp; EVALUATION DEFENSE GUIDE (ANTICIPATED QUESTIONS &amp; ANSWERS)</h2>

<div class="qa-box">
  <div class="qa-q">Q1 (For Member 1): Why does a small business need CashFlow when they already have Tally or Excel?</div>
  <div class="qa-a"><strong>Answer:</strong> Tally and traditional ERPs are backwards-looking compliance tools designed for double-entry bookkeeping, GST, and tax filing. They tell you what you spent yesterday, but they cannot tell you when your cash will breach safe operating limits next month. Excel is static, prone to formula errors, and disconnected from customer outreach. CashFlow provides forward predictive runway modeling, statistical spend anomaly radars, and instant debtor communication in a single zero-dependency cockpit.</div>
</div>

<div class="qa-box">
  <div class="qa-q">Q2 (For Member 2): Why did you choose SQLite over PostgreSQL or MySQL?</div>
  <div class="qa-a"><strong>Answer:</strong> Small business software must prioritize zero configuration and data sovereignty. Client-server databases like PostgreSQL require dedicated server processes, network port opening, and administrative overhead. SQLite is an ACID-compliant, self-contained embedded engine. By using HikariCP connection pooling and WAL (Write-Ahead Logging) mode, SQLite achieves sub-millisecond query latency and can be backed up as a single file (<code>cashflow.db</code>), while perfectly supporting the concurrency demands of SME controllers.</div>
</div>

<div class="qa-box">
  <div class="qa-q">Q3 (For Member 3): Why build a custom Canvas engine instead of importing Chart.js via CDN?</div>
  <div class="qa-a"><strong>Answer:</strong> Under Architectural Decision Record 1 (ADR 1), we prioritized zero-CDN self-containment. In retail back-offices or warehouse environments, internet connectivity can be intermittent. Third-party CDN scripts introduce external failure points, latency, and security audit concerns. Our custom <code>EnterpriseChart</code> in <code>charts.js</code> is under 30KB, renders natively on HTML5 Canvas at 60 FPS with Retina DPI scaling, and integrates seamlessly with our CSS theme switching without external downloads.</div>
</div>

<div class="qa-box">
  <div class="qa-q">Q4 (For Member 4): How does the What-If Stress Simulator ensure it doesn't corrupt actual financial records?</div>
  <div class="qa-a"><strong>Answer:</strong> The What-If simulation engine operates strictly on <strong>in-memory deep clones</strong> of the active forecast model. When a user injects a 30-day client delay or a ₹1,00,000 emergency expense shock, the mathematical solver clones the baseline closing cash array, applies the delta offsets to the projected trajectory, and returns the comparative curve directly to the client. The SQLite database remains completely untouched, ensuring absolute data immutability.</div>
</div>

<div class="qa-box">
  <div class="qa-q">Q5 (For Member 2 / Member 4): What is the mathematical basis of your Statistical Anomaly Radar?</div>
  <div class="qa-a"><strong>Answer:</strong> For each expense transaction, the system computes the historical rolling mean ($\mu$) and standard deviation ($\sigma$) within that specific accounting category. A transaction is flagged as a critical anomaly if: (1) The transaction amount exceeds 3 times the category moving average ($\ge 3\times \mu$), or (2) The statistical Z-Score exceeds 2.5 ($Z = (X - \mu) / \sigma > 2.5$). This automatically catches rogue disbursements and abnormal vendor invoices.</div>
</div>

</body>
</html>
"""

html_path = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/CashFlow_Presentation_Script_4Members.html"
docx_path = "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/CashFlow_Presentation_Script_4Members.docx"

with open(html_path, "w", encoding="utf-8") as f:
    f.write(html_content)

print(f"Wrote {html_path}")

cmd = ["/usr/bin/textutil", "-convert", "docx", html_path, "-output", docx_path]
res = subprocess.run(cmd, capture_output=True, text=True)
if res.returncode == 0:
    print(f"Successfully generated Word document: {docx_path}")
else:
    print(f"Error converting to docx: {res.stderr}")
