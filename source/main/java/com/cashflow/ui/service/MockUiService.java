package com.cashflow.ui.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.model.dto.DashboardMetrics;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 100% Standalone Mock Implementation for the Independent Frontend.
 * 
 * - NO Spring Boot
 * - NO SQLite database
 * - NO JDBC
 * - NO Backend network connections
 *
 * Provides static mock data for visual inspection and in-memory dual-party communication simulations.
 */
public class MockUiService implements CashFlowUiService {

    private final List<Transaction> mockTransactions = new ArrayList<>();
    private final List<ForecastEntry> mockForecast = new ArrayList<>();
    private final List<Warning> mockWarnings = new ArrayList<>();
    private final List<Map<String, Object>> mockAnomalies = new ArrayList<>();
    private final List<Map<String, Object>> mockRecurring = new ArrayList<>();
    private final List<Map<String, Object>> mockVersions = new ArrayList<>();
    private final List<Map<String, Object>> mockCommunicationLogs = new ArrayList<>();

    private String currentTheme = "arctic-ledger";
    private int currentForecastDays = 90;
    private BigDecimal currentThreshold = new BigDecimal("50000");
    private String currentCurrency = "INR";
    private String shopkeeperPhone = "+91 98765 00000";
    private String shopkeeperEmail = "shopkeeper@cashflow.com";

    public MockUiService() {
        initMockData();
    }

    private void initMockData() {
        LocalDate today = LocalDate.now();

        // Sample static transactions with phone & email
        Transaction t1 = new Transaction();
        t1.setTransactionId("TXN-MOCK-001");
        t1.setTransactionDate(today.minusDays(5));
        t1.setTransactionType("INCOME");
        t1.setCategory("Client Retainer");
        t1.setDescription("Enterprise Monthly Retainer Invoice");
        t1.setAmount(new BigDecimal("150000.00"));
        t1.setCustomer("Acme Corp Global");
        t1.setCustomerPhone("+91 98765 10001");
        t1.setCustomerEmail("billing@acmecorp.com");
        t1.setStatus("PAID");
        mockTransactions.add(t1);

        Transaction t2 = new Transaction();
        t2.setTransactionId("TXN-MOCK-002");
        t2.setTransactionDate(today.minusDays(2));
        t2.setTransactionType("EXPENSE");
        t2.setCategory("Office Lease");
        t2.setDescription("Headquarters Lease Payment");
        t2.setAmount(new BigDecimal("45000.00"));
        t2.setSupplier("Cyber Heights Realty");
        t2.setStatus("PAID");
        mockTransactions.add(t2);

        Transaction t3 = new Transaction();
        t3.setTransactionId("TXN-MOCK-003");
        t3.setTransactionDate(today.plusDays(3));
        t3.setTransactionType("INCOME");
        t3.setCategory("Consulting");
        t3.setDescription("Cloud Architecture Audit Fee");
        t3.setAmount(new BigDecimal("80000.00"));
        t3.setCustomer("Zenith Logistics");
        t3.setCustomerPhone("+91 98765 10002");
        t3.setCustomerEmail("accounts@zenithlogistics.com");
        t3.setDueDate(today.plusDays(10));
        t3.setStatus("PENDING");
        mockTransactions.add(t3);

        Transaction t4 = new Transaction();
        t4.setTransactionId("TXN-MOCK-004");
        t4.setTransactionDate(today.minusDays(15));
        t4.setTransactionType("INCOME");
        t4.setCategory("Software Dev");
        t4.setDescription("Milestone 2 Delivery (Delayed)");
        t4.setAmount(new BigDecimal("60000.00"));
        t4.setCustomer("Apex Retail");
        t4.setCustomerPhone("+91 98765 10003");
        t4.setCustomerEmail("finance@apexretail.com");
        t4.setDueDate(today.minusDays(4));
        t4.setStatus("OVERDUE");
        mockTransactions.add(t4);

        // Mock Forecast with realistic business fluctuations
        BigDecimal cash = new BigDecimal("105000.00");
        for (int i = 0; i < 90; i++) {
            LocalDate d = today.plusDays(i);
            BigDecimal in = BigDecimal.ZERO;
            BigDecimal out = BigDecimal.ZERO;

            if (i % 7 == 2) in = in.add(new BigDecimal("35000.00")); // Weekly collections
            if (i % 15 == 0) in = in.add(new BigDecimal("75000.00")); // Fortnightly milestone
            if (i % 30 == 5) out = out.add(new BigDecimal("45000.00")); // Monthly rent
            if (i % 30 == 1) out = out.add(new BigDecimal("60000.00")); // Monthly payroll
            if (i % 10 == 4) out = out.add(new BigDecimal("12000.00")); // Utilities/software

            cash = cash.add(in).subtract(out);
            mockForecast.add(new ForecastEntry(UUID.randomUUID().toString(), "V-MOCK", d, cash.subtract(in).add(out), in, out, cash));
        }

        // Mock Warning
        mockWarnings.add(new Warning(
                UUID.randomUUID().toString(), "V-MOCK", "OVERDUE_RECEIVABLE", "WARNING",
                today, today,
                "Overdue receivables detected totaling ₹60,000.00 from debtor: Apex Retail (+91 98765 10003). Multi-channel outreach recommended.", "ACTIVE"
        ));

        // Mock Anomaly
        Map<String, Object> a = new HashMap<>();
        a.put("transactionId", "TXN-MOCK-002");
        a.put("type", "LARGE_EXPENSE");
        a.put("expectedValue", "20000.00");
        a.put("actualValue", "45000.00");
        a.put("explanation", "[Mock Anomaly] Expense exceeds 2x historical standard deviation.");
        mockAnomalies.add(a);

        // Mock Recurring
        Map<String, Object> r1 = new HashMap<>();
        r1.put("recurringId", "REC-01");
        r1.put("description", "Office Space Lease");
        r1.put("category", "Rent");
        r1.put("amount", "45000.00");
        r1.put("frequency", "MONTHLY");
        r1.put("nextDueDate", today.plusDays(25).toString());
        mockRecurring.add(r1);

        // Mock Versions
        Map<String, Object> v1 = new HashMap<>();
        v1.put("versionId", "V-MOCK-1");
        v1.put("versionNumber", 1);
        v1.put("fileName", "mock-sample-v1.csv");
        v1.put("uploadDate", today.minusDays(10).toString());
        v1.put("recordCount", 4);
        mockVersions.add(v1);

        // Seed initial mock communication log
        Map<String, Object> initialLog = new HashMap<>();
        initialLog.put("logId", UUID.randomUUID().toString());
        initialLog.put("transactionId", "TXN-MOCK-004");
        initialLog.put("customerName", "Apex Retail");
        initialLog.put("channel", "SMS");
        initialLog.put("recipientType", "BUYER");
        initialLog.put("recipientContact", "+91 98765 10003");
        initialLog.put("subject", "Payment Reminder SMS");
        initialLog.put("message", "Payment reminder dispatched for invoice #TXN-MOCK-004.");
        initialLog.put("status", "SENT");
        initialLog.put("timestamp", LocalDateTime.now().minusHours(2).toString());
        mockCommunicationLogs.add(initialLog);
    }

    @Override
    public DashboardMetrics getDashboardMetrics() {
        DashboardMetrics m = new DashboardMetrics();
        m.setCurrentCash(new BigDecimal("105000.00"));
        m.setExpectedIncoming(new BigDecimal("140000.00"));
        m.setUpcomingExpenses(new BigDecimal("45000.00"));
        m.setNetCashFlow(new BigDecimal("95000.00"));
        m.setPendingReceivables(new BigDecimal("80000.00"));
        m.setOverdueReceivables(new BigDecimal("60000.00"));
        m.setForecastedMinCash(new BigDecimal("60000.00"));
        m.setRiskStatus("LOW");
        m.setPendingReceivablesCount(1);
        m.setOverdueCount(1);
        return m;
    }

    @Override
    public List<Transaction> getTransactions(String search, String type, String category, String status) {
        return mockTransactions;
    }

    @Override
    public List<ForecastEntry> getForecast(int days) {
        return mockForecast.subList(0, Math.min(days, mockForecast.size()));
    }

    @Override
    public List<Warning> getWarnings() {
        return mockWarnings;
    }

    @Override
    public List<Map<String, Object>> getAnomalies() {
        return mockAnomalies;
    }

    @Override
    public Map<String, Object> getReceivablesAnalysis() {
        Map<String, Object> res = new HashMap<>();
        res.put("totalOutstanding", new BigDecimal("140000.00"));
        res.put("overdueAmount", new BigDecimal("60000.00"));
        res.put("receivables", mockTransactions.stream()
                .filter(t -> "PENDING".equals(t.getStatus()) || "OVERDUE".equals(t.getStatus()))
                .toList());
        return res;
    }

    @Override
    public Map<String, Object> getExpenseAnalysis() {
        Map<String, Object> res = new HashMap<>();
        Map<String, BigDecimal> byCat = new HashMap<>();
        byCat.put("Office Lease", new BigDecimal("45000.00"));
        res.put("byCategory", byCat);
        res.put("totalExpense", new BigDecimal("45000.00"));
        return res;
    }

    @Override
    public List<Map<String, Object>> getRecurringPayments() {
        return mockRecurring;
    }

    @Override
    public void addRecurringPayment(String description, String category, BigDecimal amount, String frequency, String nextDueDate) {
        Map<String, Object> r = new HashMap<>();
        r.put("recurringId", UUID.randomUUID().toString());
        r.put("description", description);
        r.put("category", category);
        r.put("amount", amount != null ? amount.toString() : "0.00");
        r.put("frequency", frequency);
        r.put("nextDueDate", nextDueDate);
        r.put("active", true);
        mockRecurring.add(r);
    }

    @Override
    public void deleteRecurringPayment(String id) {
        mockRecurring.removeIf(r -> id != null && id.equals(r.get("recurringId")));
    }

    @Override
    public List<Map<String, Object>> getVersions() {
        return mockVersions;
    }

    @Override
    public Map<String, Object> compareVersions(String v1, String v2) {
        Map<String, Object> res = new HashMap<>();
        res.put("version1", "V1");
        res.put("version2", "V2");
        res.put("changes", Collections.emptyList());
        return res;
    }

    @Override
    public List<Map<String, Object>> getTimeline() {
        return mockVersions;
    }

    @Override
    public Map<String, Object> runScenarioSimulation(String name, String adjustmentType, BigDecimal amount, int delayDays) {
        List<ForecastEntry> baseline = mockForecast.subList(0, 30);
        List<ForecastEntry> scenario = new ArrayList<>();

        BigDecimal shock = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : new BigDecimal("30000.00");

        for (int i = 0; i < baseline.size(); i++) {
            ForecastEntry b = baseline.get(i);
            BigDecimal sClosing = b.getClosingCash();
            if ("DELAY_RECEIVABLES".equalsIgnoreCase(adjustmentType) || "DELAY_PAYMENT".equalsIgnoreCase(adjustmentType)) {
                if (i < Math.max(1, delayDays)) {
                    sClosing = sClosing.subtract(shock);
                }
            } else if ("REDUCE_EXPENSES".equalsIgnoreCase(adjustmentType) || "INCREASE_REVENUE".equalsIgnoreCase(adjustmentType)) {
                sClosing = sClosing.add(shock);
            } else {
                sClosing = sClosing.subtract(shock);
            }
            scenario.add(new ForecastEntry(UUID.randomUUID().toString(), "V-MOCK", b.getForecastDate(),
                    b.getOpeningCash(), b.getExpectedInflow(), b.getExpectedOutflow(), sClosing));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("baseline", baseline);
        res.put("scenario", scenario);
        return res;
    }

    @Override
    public CsvImportResult importCsv(File file) throws Exception {
        CsvImportResult r = new CsvImportResult();
        r.setTotalRows(10);
        r.setValidRows(10);
        r.setInvalidRows(0);
        r.setAdded(10);
        r.setModified(0);
        r.setRemoved(0);
        r.setUnchanged(0);
        r.setVersionNumber(1);
        r.setVersionId("V-MOCK-NEW");
        r.setErrors(List.of("[Mock Frontend Notice] Backend disconnected. CSV parsed in mock presentation mode only."));
        return r;
    }

    @Override
    public Map<String, Object> getSettings() {
        Map<String, Object> m = new HashMap<>();
        m.put("theme", currentTheme);
        m.put("forecastDays", currentForecastDays);
        m.put("minimumCashThreshold", currentThreshold);
        m.put("currency", currentCurrency);
        m.put("shopkeeperPhone", shopkeeperPhone);
        m.put("shopkeeperEmail", shopkeeperEmail);
        return m;
    }

    @Override
    public void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency) {
        updateSettings(theme, forecastDays, minimumThreshold, currency, this.shopkeeperPhone, this.shopkeeperEmail);
    }

    @Override
    public void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency, String shopkeeperPhone, String shopkeeperEmail) {
        this.currentTheme = theme;
        this.currentForecastDays = forecastDays;
        this.currentThreshold = minimumThreshold;
        this.currentCurrency = currency;
        if (shopkeeperPhone != null && !shopkeeperPhone.isBlank()) this.shopkeeperPhone = shopkeeperPhone;
        if (shopkeeperEmail != null && !shopkeeperEmail.isBlank()) this.shopkeeperEmail = shopkeeperEmail;
    }

    @Override
    public File exportReportCsv(File destination) throws Exception {
        try (java.io.FileWriter w = new java.io.FileWriter(destination)) {
            w.write("transaction_id,description,amount,customer,customer_phone,customer_email,status\n");
            w.write("TXN-MOCK-004,\"Milestone 2 Delivery\",60000.00,Apex Retail,+91 98765 10003,finance@apexretail.com,OVERDUE\n");
        }
        return destination;
    }

    @Override
    public Map<String, Object> initiateCall(String transactionId, String notes) {
        Transaction t = mockTransactions.stream().filter(tr -> tr.getTransactionId().equals(transactionId)).findFirst()
                .orElse(mockTransactions.get(0));
        String customer = t.getCustomer() != null ? t.getCustomer() : "Buyer";
        String buyerPhone = t.getCustomerPhone() != null ? t.getCustomerPhone() : "+91 98765 10003";

        Map<String, Object> log = new HashMap<>();
        log.put("logId", UUID.randomUUID().toString());
        log.put("transactionId", t.getTransactionId());
        log.put("customerName", customer);
        log.put("channel", "CALL");
        log.put("recipientType", "BOTH");
        log.put("recipientContact", buyerPhone + " & " + shopkeeperPhone);
        log.put("subject", "Voice Call to Buyer");
        log.put("message", "Outbound phone call initiated regarding invoice #" + t.getTransactionId() + ". Notes: " + (notes != null ? notes : "Direct reminder."));
        log.put("status", "COMPLETED");
        log.put("timestamp", LocalDateTime.now().toString());
        mockCommunicationLogs.add(0, log);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("channel", "CALL");
        res.put("transactionId", t.getTransactionId());
        res.put("customer", customer);
        res.put("buyerPhone", buyerPhone);
        res.put("shopkeeperPhone", shopkeeperPhone);
        res.put("status", "COMPLETED");
        res.put("message", "[Mock Service] Call simulated successfully to " + customer + " (" + buyerPhone + "). Logged to audit trail.");
        return res;
    }

    @Override
    public Map<String, Object> sendSmsNotice(String transactionId, String customMessage) {
        Transaction t = mockTransactions.stream().filter(tr -> tr.getTransactionId().equals(transactionId)).findFirst()
                .orElse(mockTransactions.get(0));
        String customer = t.getCustomer() != null ? t.getCustomer() : "Buyer";
        String buyerPhone = t.getCustomerPhone() != null ? t.getCustomerPhone() : "+91 98765 10003";

        Map<String, Object> log1 = new HashMap<>();
        log1.put("logId", UUID.randomUUID().toString());
        log1.put("transactionId", t.getTransactionId());
        log1.put("customerName", customer);
        log1.put("channel", "SMS");
        log1.put("recipientType", "BUYER");
        log1.put("recipientContact", buyerPhone);
        log1.put("subject", "Payment Reminder SMS");
        log1.put("message", customMessage != null ? customMessage : "Overdue reminder sent for ₹" + t.getAmount());
        log1.put("status", "SENT");
        log1.put("timestamp", LocalDateTime.now().toString());
        mockCommunicationLogs.add(0, log1);

        Map<String, Object> log2 = new HashMap<>();
        log2.put("logId", UUID.randomUUID().toString());
        log2.put("transactionId", t.getTransactionId());
        log2.put("customerName", customer);
        log2.put("channel", "SMS");
        log2.put("recipientType", "SHOPKEEPER");
        log2.put("recipientContact", shopkeeperPhone);
        log2.put("subject", "Shopkeeper Audit Copy (SMS)");
        log2.put("message", "Audit copy: Reminder dispatched to " + customer + " for invoice #" + t.getTransactionId());
        log2.put("status", "SENT");
        log2.put("timestamp", LocalDateTime.now().toString());
        mockCommunicationLogs.add(0, log2);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("channel", "SMS");
        res.put("transactionId", t.getTransactionId());
        res.put("customer", customer);
        res.put("buyerPhone", buyerPhone);
        res.put("shopkeeperPhone", shopkeeperPhone);
        res.put("status", "SENT");
        res.put("message", "[Mock Service] SMS sent to both Buyer (" + buyerPhone + ") and Shopkeeper (" + shopkeeperPhone + ").");
        return res;
    }

    @Override
    public Map<String, Object> sendEmailNotice(String transactionId, String customSubject, String customBody) {
        Transaction t = mockTransactions.stream().filter(tr -> tr.getTransactionId().equals(transactionId)).findFirst()
                .orElse(mockTransactions.get(0));
        String customer = t.getCustomer() != null ? t.getCustomer() : "Buyer";
        String buyerEmail = t.getCustomerEmail() != null ? t.getCustomerEmail() : "finance@buyer.com";

        Map<String, Object> log = new HashMap<>();
        log.put("logId", UUID.randomUUID().toString());
        log.put("transactionId", t.getTransactionId());
        log.put("customerName", customer);
        log.put("channel", "EMAIL");
        log.put("recipientType", "BOTH");
        log.put("recipientContact", buyerEmail + " & " + shopkeeperEmail);
        log.put("subject", customSubject != null ? customSubject : "Formal Payment Notice for Invoice #" + t.getTransactionId());
        log.put("message", customBody != null ? customBody : "Official overdue invoice notice sent.");
        log.put("status", "SENT");
        log.put("timestamp", LocalDateTime.now().toString());
        mockCommunicationLogs.add(0, log);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("channel", "EMAIL");
        res.put("transactionId", t.getTransactionId());
        res.put("customer", customer);
        res.put("buyerEmail", buyerEmail);
        res.put("shopkeeperEmail", shopkeeperEmail);
        res.put("status", "SENT");
        res.put("message", "[Mock Service] Email notice dispatched to both Buyer (" + buyerEmail + ") and Shopkeeper (" + shopkeeperEmail + ").");
        return res;
    }

    @Override
    public Map<String, Object> notifyBothParties(String transactionId) {
        sendSmsNotice(transactionId, null);
        sendEmailNotice(transactionId, null, null);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("transactionId", transactionId);
        res.put("message", "[Mock Service] 1-Click Multi-Channel Notification: SMS & Email sent to both Buyer and Shopkeeper!");
        return res;
    }

    @Override
    public Map<String, Object> bulkNotifyDefaulters() {
        int count = 0;
        List<String> notified = new ArrayList<>();
        for (Transaction t : mockTransactions) {
            if ("OVERDUE".equalsIgnoreCase(t.getStatus())) {
                notifyBothParties(t.getTransactionId());
                notified.add(t.getTransactionId());
                count++;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("totalOverdue", count);
        res.put("notifiedCount", count);
        res.put("transactions", notified);
        res.put("message", "[Mock Service] Alerted " + count + " overdue defaulters via SMS & Email.");
        return res;
    }

    @Override
    public List<Map<String, Object>> getCommunicationLogs(String transactionId) {
        if (transactionId == null || transactionId.isBlank() || "ALL".equalsIgnoreCase(transactionId)) {
            return mockCommunicationLogs;
        }
        return mockCommunicationLogs.stream()
                .filter(l -> transactionId.equals(l.get("transactionId")))
                .toList();
    }
}
