# Independent Frontend User & Setup Guide

---

## 1. Overview

`frontend-only/` is an **independent, zero-backend, zero-database** pure Java desktop application built using **JavaFX (Java 21)** and **Maven**.

It reproduces the full visual interface, navigation, and analytics dashboard of the CashFlow Early Warning System while being completely isolated from Spring Boot, SQLite, and network services.

---

## 2. Quick Start

### Prerequisites
* **Java**: OpenJDK 21+
* **Build Tool**: Apache Maven 3.8+

### Launching the Application
```bash
cd "/Users/vedansh/Desktop/VS CODE SAVED/CASH_IN/frontend-only"

# Compile and run native desktop window
mvn javafx:run
```

---

## 3. Screen Navigation & Features

* **Dashboard**:
  * 8 Financial KPI Cards (Current Cash, Expected Inflow, Upcoming Expenses, Net Flow, Overdue Receivables, Risk Assessment).
  * Multicolored semantic **Bar Chart** (Sky Blue, Emerald Green, Rose, Purple).
  * Jewel-toned **Receivables Aging Pie Chart**.
* **Transactions**:
  * Filterable ledger table with search, type filter, status filter, and live record count.
* **Forecast**:
  * Interactive cash trajectory Line Chart selectable across 7, 30, 60, and 90 days.
* **Warnings & Anomalies**:
  * Explainable early warning alerts and statistical anomaly detection list.
  * Direct "Contact Defaulter" quick action button on overdue alerts.
* **Receivables & Multi-Channel Defaulter Outreach**:
  * Aging analysis and receivables ledger displaying Buyer Phone and Buyer Email columns.
  * 📞 **Call**: Outbound phone call dialog displaying buyer number, debt sum, overdue days, and call script.
  * 💬 **SMS**: Send SMS reminder modal with pre-filled invoice reminder and delivery simulation.
  * ✉️ **Email**: Send Email notice modal with formal invoice itemization and payment remittance details.
  * 🔄 **Notify Both**: Simultaneous dual-party notification dispatching reminders to both the Buyer and the Shopkeeper.
  * 🚨 **Alert All Defaulters**: Single-click bulk outreach across all overdue customer accounts.
  * 📜 **Communication Audit History**: Modal viewing full chronological audit log of all outreach attempts.
* **Communication & Multi-Channel Outreach Page**:
  * Dedicated customer relations and collection command center.
  * Direct table display of customer **Mobile Number** and **Email Address** with formatted contact pills.
  * Quick-action buttons (📞 Call, 💬 SMS, ✉️ Email, 🔄 Both) and real-time search across customer names, phone numbers, and emails.
  * Dual-tab layout: Customer Directory vs Permanent Live Communication Audit Trail ledger.
  * ⚡ **Alert All Defaulters**: One-click batch outreach across all overdue accounts.
* **Expenses**:
  * Categorized spending pie chart with scheduled recurring commitments.
* **What-If Simulator**:
  * Test hypothetical shocks (e.g. ₹50,000 delayed payments) and visually compare simulated vs baseline trajectory.
* **Settings & Themes**:
  * Shopkeeper profile configuration (Shopkeeper Phone and Shopkeeper Email) used for dual-party outreach.
  * Real-time dynamic theme switching between 5 high-contrast palettes (*Arctic Ledger*, *Forest Ledger*, *Copper Desk*, *Ocean Glass*, *Paper & Ink*).

---

## 4. Decoupled Architecture

All visual components connect exclusively through `CashFlowUiService.java` using `MockUiService.java`:
* Clicking action buttons (*Call*, *SMS*, *Email*, *Notify Both*, *Save Settings*, *Run What-If*, *Add Recurring Payment*) performs in-memory simulation and mock logging with zero database or network dependencies.
* Demonstrates that the UI is 100% decoupled and ready for any backend integration.
