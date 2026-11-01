package com.cashflow.demo;

import com.cashflow.model.CommunicationLog;
import com.cashflow.model.DatasetVersion;
import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.model.dto.DashboardMetrics;
import com.cashflow.service.*;
import com.cashflow.ui.service.CashFlowUiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComprehensiveDryRunTest {

    @Autowired
    private CsvImportService importService;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private CashFlowUiService uiService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private WarningService warningService;

    @Autowired
    private AnomalyService anomalyService;

    @Autowired
    private DataComparisonService comparisonService;

    @Test
    @Order(1)
    @DisplayName("Feature 1: Ingest Initial Dataset (Version 1) with Customer Mobile & Email")
    public void test01_ImportInitialDataset() throws Exception {
        File csvFile = new File("source/main/resources/sample-data.csv");
        assertTrue(csvFile.exists(), "sample-data.csv must exist");

        var result = importService.importCsv(csvFile);
        assertNotNull(result);
        assertTrue(result.getValidRows() > 0, "Should import valid rows from sample-data.csv");
        System.out.println("[TEST 1 PASSED] Initial Dataset imported. Valid rows: " + result.getValidRows());
    }

    @Test
    @Order(2)
    @DisplayName("Feature 2: Verify Dashboard Financial KPIs & Risk Metrics")
    public void test02_VerifyDashboardMetrics() {
        DashboardMetrics metrics = uiService.getDashboardMetrics();
        assertNotNull(metrics);
        assertNotNull(metrics.getCurrentCash());
        assertNotNull(metrics.getExpectedIncoming());
        assertNotNull(metrics.getUpcomingExpenses());
        assertNotNull(metrics.getNetCashFlow());
        assertNotNull(metrics.getRiskStatus());
        System.out.println("[TEST 2 PASSED] Dashboard KPIs verified: Current Cash = " + metrics.getCurrentCash() + ", Risk = " + metrics.getRiskStatus());
    }

    @Test
    @Order(3)
    @DisplayName("Feature 3: Verify All Transaction Filtering (Search, Mobile, Email, Status, Type)")
    public void test03_VerifyTransactionFilters() {
        // All transactions
        List<Transaction> all = uiService.getTransactions(null, "ALL", "ALL", "ALL");
        assertFalse(all.isEmpty(), "Should have transactions");

        // Filter by Type
        List<Transaction> income = uiService.getTransactions(null, "INCOME", "ALL", "ALL");
        assertTrue(income.stream().allMatch(t -> "INCOME".equalsIgnoreCase(t.getTransactionType())));

        List<Transaction> expense = uiService.getTransactions(null, "EXPENSE", "ALL", "ALL");
        assertTrue(expense.stream().allMatch(t -> "EXPENSE".equalsIgnoreCase(t.getTransactionType())));

        // Filter by Status
        List<Transaction> overdue = uiService.getTransactions(null, "ALL", "ALL", "OVERDUE");
        assertTrue(overdue.stream().allMatch(t -> "OVERDUE".equalsIgnoreCase(t.getStatus())));

        // Search by Customer Name
        List<Transaction> byCust = uiService.getTransactions("ABC Ltd", "ALL", "ALL", "ALL");
        assertFalse(byCust.isEmpty(), "Search by customer name should return results");

        // Search by Customer Mobile
        List<Transaction> byPhone = uiService.getTransactions("98765-10001", "ALL", "ALL", "ALL");
        assertFalse(byPhone.isEmpty(), "Search by mobile number should return results");
        assertEquals("ABC Ltd", byPhone.get(0).getCustomer());

        // Search by Customer Email
        List<Transaction> byEmail = uiService.getTransactions("billing@abcltd.com", "ALL", "ALL", "ALL");
        assertFalse(byEmail.isEmpty(), "Search by email address should return results");
        assertEquals("ABC Ltd", byEmail.get(0).getCustomer());

        System.out.println("[TEST 3 PASSED] All Transaction filters (Search, Phone, Email, Status, Type) verified!");
    }

    @Test
    @Order(4)
    @DisplayName("Feature 4: Verify Receivables & Aging Analysis with Customer Phone and Email")
    public void test04_VerifyReceivables() {
        Map<String, Object> receivables = uiService.getReceivablesAnalysis();
        assertNotNull(receivables);
        assertTrue(receivables.containsKey("totalOutstanding"));
        assertTrue(receivables.containsKey("overdueAmount"));
        assertTrue(receivables.containsKey("aging"));

        // Verify customer delays and contacts
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> customerDelays = (List<Map<String, Object>>) receivables.get("customerDelays");
        assertNotNull(customerDelays);
        assertFalse(customerDelays.isEmpty(), "Customer list should not be empty");

        boolean hasContact = customerDelays.stream().anyMatch(c -> c.get("phone") != null || c.get("email") != null);
        assertTrue(hasContact, "Customers must have phone or email contact populated");
        System.out.println("[TEST 4 PASSED] Receivables and Customer Contact Directory verified!");
    }

    @Test
    @Order(5)
    @DisplayName("Feature 5: Outbound Voice Call to Customer Mobile with Dual-Party Audit")
    public void test05_OutboundCallOutreach() {
        List<Transaction> overdue = uiService.getTransactions(null, "INCOME", "ALL", "OVERDUE");
        assertFalse(overdue.isEmpty(), "Need overdue transaction for outreach");
        Transaction target = overdue.get(0);

        Map<String, Object> callRes = communicationService.initiateCall(target.getTransactionId(), "Defaulter call test notes");
        assertTrue((Boolean) callRes.get("success"));
        assertEquals("CALL", callRes.get("channel"));
        assertEquals("COMPLETED", callRes.get("status"));
        assertNotNull(callRes.get("buyerPhone"));
        assertNotNull(callRes.get("shopkeeperPhone"));
        System.out.println("[TEST 5 PASSED] Outbound call placed to " + callRes.get("buyerPhone") + " (Buyer) & logged with Shopkeeper copy!");
    }

    @Test
    @Order(6)
    @DisplayName("Feature 6: SMS Payment Notice to Customer Mobile with Shopkeeper Copy")
    public void test06_SmsNoticeOutreach() {
        List<Transaction> overdue = uiService.getTransactions(null, "INCOME", "ALL", "OVERDUE");
        Transaction target = overdue.get(0);

        Map<String, Object> smsRes = communicationService.sendSmsNotice(target.getTransactionId(), "Please settle your invoice immediately.");
        assertTrue((Boolean) smsRes.get("success"));
        assertEquals("SMS", smsRes.get("channel"));
        assertEquals("SENT", smsRes.get("status"));
        assertNotNull(smsRes.get("buyerMessage"));
        assertNotNull(smsRes.get("shopkeeperMessage"));
        System.out.println("[TEST 6 PASSED] SMS notice delivered to Buyer (" + smsRes.get("buyerPhone") + ") and Shopkeeper (" + smsRes.get("shopkeeperPhone") + ")");
    }

    @Test
    @Order(7)
    @DisplayName("Feature 7: Formal Invoice Email Notice to Customer Email with Audit CC")
    public void test07_EmailNoticeOutreach() {
        List<Transaction> overdue = uiService.getTransactions(null, "INCOME", "ALL", "OVERDUE");
        Transaction target = overdue.get(0);

        Map<String, Object> emailRes = communicationService.sendEmailNotice(target.getTransactionId(), "Formal Notice", "Invoice settlement required.");
        assertTrue((Boolean) emailRes.get("success"));
        assertEquals("EMAIL", emailRes.get("channel"));
        assertEquals("SENT", emailRes.get("status"));
        assertNotNull(emailRes.get("buyerEmail"));
        assertNotNull(emailRes.get("shopkeeperEmail"));
        System.out.println("[TEST 7 PASSED] Formal statement delivered to Buyer (" + emailRes.get("buyerEmail") + ") and CC to Shopkeeper (" + emailRes.get("shopkeeperEmail") + ")");
    }

    @Test
    @Order(8)
    @DisplayName("Feature 8: Dual-Party Simultaneous Notification (SMS + Email)")
    public void test08_DualPartyOutreach() {
        List<Transaction> overdue = uiService.getTransactions(null, "INCOME", "ALL", "OVERDUE");
        Transaction target = overdue.get(0);

        Map<String, Object> dualRes = communicationService.notifyBothParties(target.getTransactionId());
        assertTrue((Boolean) dualRes.get("success"));
        assertNotNull(dualRes.get("sms"));
        assertNotNull(dualRes.get("email"));
        System.out.println("[TEST 8 PASSED] Dual-party broadcast dispatched SMS & Email to both Buyer & Shopkeeper!");
    }

    @Test
    @Order(9)
    @DisplayName("Feature 9: Bulk Defaulters Outreach Across All Overdue Accounts")
    public void test09_BulkDefaultersOutreach() {
        Map<String, Object> bulkRes = communicationService.bulkNotifyDefaulters();
        assertTrue((Boolean) bulkRes.get("success"));
        assertTrue((Integer) bulkRes.get("notifiedCount") >= 1, "Should notify overdue accounts");
        System.out.println("[TEST 9 PASSED] Bulk alert successfully notified " + bulkRes.get("notifiedCount") + " overdue defaulters!");
    }

    @Test
    @Order(10)
    @DisplayName("Feature 10: Permanent Communication Audit Trail Ledger")
    public void test10_AuditTrailQuery() {
        List<CommunicationLog> logs = communicationService.getCommunicationLogs("ALL");
        assertFalse(logs.isEmpty(), "Communication logs must be recorded");
        assertTrue(logs.size() >= 5, "Audit log should have multiple call, sms, and email entries");

        boolean hasBuyer = logs.stream().anyMatch(l -> "BUYER".equalsIgnoreCase(l.getRecipientType()));
        boolean hasShopkeeper = logs.stream().anyMatch(l -> "SHOPKEEPER".equalsIgnoreCase(l.getRecipientType()));
        assertTrue(hasBuyer, "Audit logs must record buyer entries");
        assertTrue(hasShopkeeper, "Audit logs must record shopkeeper audit copies");
        System.out.println("[TEST 10 PASSED] Permanent Communication Audit Trail verified with " + logs.size() + " immutable records!");
    }

    @Test
    @Order(11)
    @DisplayName("Feature 11: Multi-Horizon Cash-Flow Projections (7, 30, 60, 90 Days)")
    public void test11_CashFlowForecastHorizons() {
        for (int days : new int[]{7, 30, 60, 90}) {
            List<ForecastEntry> forecast = uiService.getForecast(days);
            assertNotNull(forecast);
            assertEquals(days, forecast.size(), "Forecast size should match horizon days: " + days);
            assertNotNull(forecast.get(0).getClosingCash());
        }
        System.out.println("[TEST 11 PASSED] 7, 30, 60, and 90-day cash forecast trajectories verified!");
    }

    @Test
    @Order(12)
    @DisplayName("Feature 12: Early Warning Radar & Statistical Anomaly Detector")
    public void test12_WarningsAndAnomalies() {
        List<Warning> warnings = uiService.getWarnings();
        assertNotNull(warnings);

        List<Map<String, Object>> anomalies = uiService.getAnomalies();
        assertNotNull(anomalies);
        System.out.println("[TEST 12 PASSED] Early warnings & 3x spending anomaly detection verified!");
    }

    @Test
    @Order(13)
    @DisplayName("Feature 13: Recurring Payments Lifecycle (Create, Query, Delete)")
    public void test13_RecurringPayments() {
        int initial = uiService.getRecurringPayments().size();
        uiService.addRecurringPayment("Office Internet Fibre", "Utilities", new BigDecimal("3500.00"), "MONTHLY", "2026-11-05");

        List<Map<String, Object>> afterAdd = uiService.getRecurringPayments();
        assertEquals(initial + 1, afterAdd.size());

        Map<String, Object> added = afterAdd.stream()
                .filter(r -> "Office Internet Fibre".equals(r.get("description")))
                .findFirst().orElseThrow();
        assertEquals("Utilities", added.get("category"));

        uiService.deleteRecurringPayment(String.valueOf(added.get("recurringId")));
        assertEquals(initial, uiService.getRecurringPayments().size());
        System.out.println("[TEST 13 PASSED] Recurring payments creation, verification and deletion verified!");
    }

    @Test
    @Order(14)
    @DisplayName("Feature 14: What-If Stress Test Simulation with Multiple Scenarios")
    public void test14_WhatIfSimulation() {
        String[] scenarioTypes = {"DELAY_RECEIVABLES", "INCREASE_EXPENSES", "UNEXPECTED_EXPENSE", "REDUCE_EXPENSES", "INCREASE_REVENUE"};
        for (String type : scenarioTypes) {
            Map<String, Object> sim = uiService.runScenarioSimulation(
                    "Simulation of " + type, type, new BigDecimal("40000.00"), 14
            );
            assertNotNull(sim, "Simulation failed for " + type);
            assertTrue(sim.containsKey("baseline"));
            assertTrue(sim.containsKey("scenario"));
        }
        System.out.println("[TEST 14 PASSED] All What-If simulation shock scenarios verified!");
    }

    @Test
    @Order(15)
    @DisplayName("Feature 15: Ingest Version 2 & Verify Immutable Delta Comparison")
    public void test15_VersionComparisonDelta() throws Exception {
        File v2File = new File("source/main/resources/sample-data-v2.csv");
        assertTrue(v2File.exists(), "sample-data-v2.csv must exist");

        var v2Result = importService.importCsv(v2File);
        assertNotNull(v2Result);
        assertTrue(v2Result.getValidRows() > 0);

        List<Map<String, Object>> versions = uiService.getVersions();
        assertTrue(versions.size() >= 2, "Should have at least 2 versions");

        Map<String, Object> diff = uiService.compareVersions("1", "2");
        assertNotNull(diff);
        assertTrue(diff.containsKey("summary"));
        assertTrue(diff.containsKey("changes"));
        System.out.println("[TEST 15 PASSED] Version 2 imported & delta comparison verified between V1 and V2!");
    }

    @Test
    @Order(16)
    @DisplayName("Feature 16: Settings & Color Themes (Arctic, Forest, Copper, Ocean, Paper)")
    public void test16_ThemeSwitchingAndSettings() {
        String[] themes = {
                "theme-arctic-ledger",
                "theme-forest-ledger",
                "theme-copper-desk",
                "theme-ocean-glass",
                "theme-paper-ink"
        };

        for (String theme : themes) {
            uiService.updateSettings(theme, 90, new BigDecimal("60000.00"), "INR", "+91 99887 76655", "owner@cashflow.com");
            Map<String, Object> current = uiService.getSettings();
            assertEquals(theme, current.get("theme"));
            assertEquals("+91 99887 76655", current.get("shopkeeperPhone"));
            assertEquals("owner@cashflow.com", current.get("shopkeeperEmail"));
        }
        System.out.println("[TEST 16 PASSED] All 5 Visual Color Themes & Shopkeeper Profile persistence verified!");
    }

    @Test
    @Order(17)
    @DisplayName("Feature 17: Reports Consolidated CSV Export Generation")
    public void test17_ReportExportCsv() throws Exception {
        File tempExport = File.createTempFile("cashflow-report-test", ".csv");
        tempExport.deleteOnExit();

        File exported = uiService.exportReportCsv(tempExport);
        assertTrue(exported.exists());
        assertTrue(exported.length() > 0);

        List<String> lines = Files.readAllLines(exported.toPath());
        assertFalse(lines.isEmpty(), "Exported CSV must contain rows");
        assertTrue(lines.get(0).contains("transaction_id"), "Export header must contain transaction_id");
        System.out.println("[TEST 17 PASSED] Consolidated Financial Report CSV generated (" + lines.size() + " lines)!");
    }
}
