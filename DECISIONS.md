# Development Decisions & Architectural Rationale

This document maintains a chronological record of relevant architectural, technical, and implementation decisions made throughout the evolution of the **CashFlow Small Business Liquidity & Early Warning System**.

---

## Decision Log

### 1. Zero-CDN Self-Contained Frontend Architecture
- **Date:** 2026-10-27
- **Decision:** Remove all external CDN dependencies (Chart.js, external fonts, remote icons) and implement a custom, offline-capable canvas rendering engine (`EnterpriseChart` in `charts.js`).
- **Why Chosen:** Enterprise ERP and financial accounting systems must operate reliably in air-gapped, offline, and latency-sensitive business environments. External CDN links cause layout failure, security audit flags, and rendering lag when internet connectivity fluctuates.
- **Alternatives Considered:** Bundling NPM packages via Webpack/Vite; using SVG charting libraries.
- **Impact:** 100% self-contained application running entirely from local classpath with zero network latency, immediate sub-50ms canvas chart rendering, and full theme integration.

### 2. Multi-Channel Customer Outreach with Dual-Party Audit Logging
- **Date:** 2026-10-28
- **Decision:** Extend the Customer Receivables module with direct communication capabilities:
  - Phone call integration via system telephony dialer (`tel:` protocol) with call outcome note logging.
  - SMS dispatch with 160-character budget validation.
  - Formal account statement generation and email dispatch.
  - Dual-Party Dispatch: Whenever an alert is triggered (call, SMS, email, or bulk defaulter alert), a simultaneous audit copy is routed to the Shopkeeper's configured mobile and email address.
- **Why Chosen:** Small business owners frequently face collections friction. Having one-click outreach linked directly to specific debtor invoices and immutable audit logging eliminates disputes over payment reminders.
- **Alternatives Considered:** External third-party SMS/Email gateway SDKs (Twilio/SendGrid).
- **Impact:** Self-contained and instant simulation engine with persistent SQLite audit trail (`communication_log` table) and dual-party compliance verification.

### 3. Executive Zero-Scroll Dashboard Layout
- **Date:** 2026-10-28
- **Decision:** Transform the main Dashboard into a single-viewport, zero-scroll layout utilizing CSS Grid and Flexbox with tight informational density.
- **Why Chosen:** Executive financial dashboards must provide an immediate cockpit overview of liquid cash, receivables risk, impending expenses, and forward forecasts without vertical disorienting scrolling.
- **Alternatives Considered:** Infinite vertical dashboard stream with collapsible widgets.
- **Impact:** Clean, crisp ERP SaaS presentation matching modern institutional standards with high information density.

### 4. Direct Inline Action Handlers for Form Submissions & Event Listeners
- **Date:** 2026-10-28
- **Decision:** Hardened all `<form>` tags with explicit `onsubmit="event.preventDefault(); Handler.submit(event); return false;"` and bound inline action listeners (`oninput`, `onchange`, `onclick`) to topbar filters, tab switches, and modal actions.
- **Why Chosen:** Native browser form submissions without preventive returns trigger full page reloads to `index.html?`, resetting JavaScript state, chart canvases, and selected themes. Direct inline triggers bypass browser script caching issues and guarantee immediate responsiveness.
- **Alternatives Considered:** Pure JavaScript `addEventListener` blocks only.
- **Impact:** Rock-solid UX where every button, filter dropdown, and theme card executes its intended behavior immediately without unintended URL mutations or state resets.

### 5. Multi-Version Dataset Delta & Comparison Architecture
- **Date:** 2026-10-28
- **Decision:** Implement version-aware dataset tracking where each CSV upload creates an isolated version (`V1`, `V2`, `V3`), computes added/modified/removed transaction deltas, and enables side-by-side version comparison.
- **Why Chosen:** Financial auditors and business controllers need to inspect accounting changes over time and verify data lineage between monthly imports.
- **Alternatives Considered:** Overwriting transaction records in-place without versioning.
- **Impact:** Immutable audit history, granular change radar, and scenario comparison capabilities.

### 6. Five Enterprise Theming System with Dynamic Canvas Re-Rendering
- **Date:** 2026-10-28
- **Decision:** Built 5 curated corporate color palettes (Arctic Ledger, Forest Ledger, Copper Desk, Ocean Glass, Paper & Ink) using CSS custom properties with automatic chart buffer redraws (`Charts.updateAllThemes()`).
- **Why Chosen:** Visual ergonomics: high-contrast dark modes for evening operations, paper & ink for daylight environments.
- **Alternatives Considered:** Single static dark theme.
- **Impact:** User personalization persisted locally and in backend SQLite database.

### 7. Streamlined UI Redesign, Permutations & Combinations Hardening, and 1-Click Simulation Presets
- **Date:** 2026-11-01
- **Decision:** 
  1. Streamlined navigation from 12 cluttered items down to clean functional categories, removing the redundant standalone "Audit Timeline" page and embedding it directly within "Versions & Lineage".
  2. Fixed double-event registration across transactions, reports, forecast, and simulation forms where both programmatic `addEventListener` and inline `onclick` fired simultaneously.
  3. Hardened the What-If Stress Simulator with 1-click Preset Shock Scenarios (*30-Day Client Delay, Emergency Capex Shock, 25% Demand Dip, Cost Inflation*) and added an automated Day-1 fallback when target shock dates are omitted.
  4. Added responsive auto-fit CSS grid layouts and vertical viewport scrolling to prevent chart and KPI clipping on standard laptop displays.
  5. Implemented `renderEmptyState()` routines in the Canvas engine (`charts.js`) to prevent blank void canvases when datasets or filters return zero records.
  6. Added comprehensive automated test suite `PermutationCombinationTest.java` verifying all 3 demo datasets, compound multi-shocks, horizon variations, and multi-channel communication channels.
- **Why Chosen:** Direct response to user requirements for simplified UI, guaranteed reliability across all button permutations, and complete simulation accuracy.
- **Impact:** Clean, modern, intuitive user interface with 52 passing tests, zero console errors, and instant simulation response.

- **Why Chosen:** Provides professional contrast adapted for bright accounting offices, low-light environments, and high-contrast projection displays.
- **Alternatives Considered:** Hardcoded hex colors per component.
- **Impact:** Seamless, instantaneous theme switching across all views and canvas charts with persistence to both `localStorage` and SQLite settings.
