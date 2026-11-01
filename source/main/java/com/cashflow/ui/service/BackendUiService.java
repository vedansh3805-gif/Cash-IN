package com.cashflow.ui.service;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.model.CommunicationLog;
import com.cashflow.model.DatasetVersion;
import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.model.dto.DashboardMetrics;
import com.cashflow.repository.*;
import com.cashflow.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of CashFlowUiService backed by Spring Boot services and SQLite.
 */
@Service
public class BackendUiService implements CashFlowUiService {

    private final CashFlowService cashFlowService;
    private final TransactionRepository transactionRepository;
    private final DatasetVersionRepository versionRepository;
    private final ForecastService forecastService;
    private final WarningRepository warningRepository;
    private final AnomalyRepository anomalyRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;
    private final SettingsRepository settingsRepository;
    private final CsvImportService csvImportService;
    private final CommunicationService communicationService;
    private final JdbcTemplate jdbcTemplate;

    public BackendUiService(CashFlowService cashFlowService,
                            TransactionRepository transactionRepository,
                            DatasetVersionRepository versionRepository,
                            ForecastService forecastService,
                            WarningRepository warningRepository,
                            AnomalyRepository anomalyRepository,
                            RecurringPaymentRepository recurringPaymentRepository,
                            SettingsRepository settingsRepository,
                            CsvImportService csvImportService,
                            CommunicationService communicationService,
                            JdbcTemplate jdbcTemplate) {
        this.cashFlowService = cashFlowService;
        this.transactionRepository = transactionRepository;
        this.versionRepository = versionRepository;
        this.forecastService = forecastService;
        this.warningRepository = warningRepository;
        this.anomalyRepository = anomalyRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.settingsRepository = settingsRepository;
        this.csvImportService = csvImportService;
        this.communicationService = communicationService;
        this.jdbcTemplate = jdbcTemplate;
    }

    private String getLatestVersionId() {
        return versionRepository.findLatest().map(DatasetVersion::getVersionId).orElse(null);
    }

    @Override
    public DashboardMetrics getDashboardMetrics() {
        String versionId = getLatestVersionId();
        if (versionId == null) {
            DashboardMetrics m = new DashboardMetrics();
            m.setCurrentCash(BigDecimal.ZERO);
            m.setExpectedIncoming(BigDecimal.ZERO);
            m.setUpcomingExpenses(BigDecimal.ZERO);
            m.setNetCashFlow(BigDecimal.ZERO);
            m.setRiskStatus("LOW");
            return m;
        }
        return cashFlowService.calculateMetrics(versionId);
    }

    @Override
    public List<Transaction> getTransactions(String search, String type, String category, String status) {
        String versionId = getLatestVersionId();
        if (versionId == null) return Collections.emptyList();

        List<Transaction> list = transactionRepository.findAllByVersionId(versionId);
        return list.stream().filter(t -> {
            if (type != null && !type.equalsIgnoreCase("ALL") && !type.equalsIgnoreCase(t.getTransactionType())) return false;
            if (category != null && !category.equalsIgnoreCase("ALL") && !category.equalsIgnoreCase(t.getCategory())) return false;
            if (status != null && !status.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase(t.getStatus())) return false;
            if (search != null && !search.trim().isEmpty()) {
                String s = search.toLowerCase();
                boolean matches = (t.getDescription() != null && t.getDescription().toLowerCase().contains(s))
                        || (t.getCustomer() != null && t.getCustomer().toLowerCase().contains(s))
                        || (t.getCustomerPhone() != null && t.getCustomerPhone().toLowerCase().contains(s))
                        || (t.getCustomerEmail() != null && t.getCustomerEmail().toLowerCase().contains(s))
                        || (t.getSupplier() != null && t.getSupplier().toLowerCase().contains(s))
                        || (t.getTransactionId() != null && t.getTransactionId().toLowerCase().contains(s));
                if (!matches) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ForecastEntry> getForecast(int days) {
        String versionId = getLatestVersionId();
        if (versionId == null) return Collections.emptyList();
        return forecastService.generateForecast(versionId, days);
    }

    @Override
    public List<Warning> getWarnings() {
        String versionId = getLatestVersionId();
        if (versionId == null) return Collections.emptyList();
        return warningRepository.findByVersionId(versionId);
    }

    @Override
    public List<Map<String, Object>> getAnomalies() {
        String versionId = getLatestVersionId();
        if (versionId == null) return Collections.emptyList();
        return anomalyRepository.findByVersionId(versionId).stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("anomalyId", a.getAnomalyId());
            m.put("transactionId", a.getTransactionId());
            m.put("type", a.getAnomalyType());
            m.put("severity", a.getSeverity());
            m.put("expectedValue", a.getExpectedValue());
            m.put("actualValue", a.getActualValue());
            m.put("explanation", a.getExplanation());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getReceivablesAnalysis() {
        String versionId = getLatestVersionId();
        Map<String, Object> res = new HashMap<>();
        if (versionId == null) {
            res.put("totalOutstanding", BigDecimal.ZERO);
            res.put("overdueAmount", BigDecimal.ZERO);
            res.put("averagePaymentDelay", 0.0);
            res.put("aging", Collections.emptyMap());
            res.put("receivables", Collections.emptyList());
            res.put("customerDelays", Collections.emptyList());
            return res;
        }

        List<Transaction> receivables = transactionRepository.findAllByVersionId(versionId).stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getTransactionType()) && ("PENDING".equalsIgnoreCase(t.getStatus()) || "OVERDUE".equalsIgnoreCase(t.getStatus())))
                .collect(Collectors.toList());

        BigDecimal total = receivables.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overdue = receivables.stream().filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus())).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days30 = BigDecimal.ZERO;
        BigDecimal days60 = BigDecimal.ZERO;
        BigDecimal days90 = BigDecimal.ZERO;
        BigDecimal over90 = BigDecimal.ZERO;

        for (Transaction r : receivables) {
            BigDecimal amt = r.getAmount();
            if (r.getDueDate() != null) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), today);
                if (daysOverdue <= 0) current = current.add(amt);
                else if (daysOverdue <= 30) days30 = days30.add(amt);
                else if (daysOverdue <= 60) days60 = days60.add(amt);
                else if (daysOverdue <= 90) days90 = days90.add(amt);
                else over90 = over90.add(amt);
            }
        }

        Map<String, BigDecimal> aging = new HashMap<>();
        aging.put("current", current);
        aging.put("days30", days30);
        aging.put("days60", days60);
        aging.put("days90", days90);
        aging.put("over90", over90);

        List<Map<String, Object>> customerDelays = jdbcTemplate.queryForList("SELECT customer_name as customerName, phone, email, average_payment_delay as averageDelay, total_outstanding as totalOutstanding FROM customer ORDER BY average_payment_delay DESC");
        Double avgDelay = 0.0;
        try {
            avgDelay = jdbcTemplate.queryForObject("SELECT AVG(average_payment_delay) FROM customer WHERE average_payment_delay > 0", Double.class);
        } catch (Exception ignored) {}

        res.put("totalOutstanding", total);
        res.put("overdueAmount", overdue);
        res.put("averagePaymentDelay", avgDelay != null ? avgDelay : 0.0);
        res.put("aging", aging);
        res.put("receivables", receivables);
        res.put("customerDelays", customerDelays);
        return res;
    }

    @Override
    public Map<String, Object> getExpenseAnalysis() {
        String versionId = getLatestVersionId();
        Map<String, Object> res = new HashMap<>();
        if (versionId == null) {
            res.put("byCategory", Collections.emptyMap());
            res.put("totalExpense", BigDecimal.ZERO);
            return res;
        }

        List<Transaction> expenses = transactionRepository.findAllByVersionId(versionId).stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList());

        Map<String, BigDecimal> byCat = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : expenses) {
            String cat = t.getCategory() != null ? t.getCategory() : "Other";
            byCat.put(cat, byCat.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
            total = total.add(t.getAmount());
        }

        res.put("byCategory", byCat);
        res.put("totalExpense", total);
        return res;
    }

    @Override
    public List<Map<String, Object>> getRecurringPayments() {
        return recurringPaymentRepository.findAll().stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("recurringId", r.getRecurringId());
            m.put("description", r.getDescription());
            m.put("category", r.getCategory());
            m.put("amount", r.getAmount());
            m.put("frequency", r.getFrequency());
            m.put("nextDueDate", r.getNextDueDate());
            m.put("active", r.getActive());
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public void addRecurringPayment(String description, String category, BigDecimal amount, String frequency, String nextDueDate) {
        String sql = "INSERT INTO recurring_payment (recurring_id, description, category, amount, frequency, next_due_date, active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, UUID.randomUUID().toString(), description, category, amount, frequency, LocalDate.parse(nextDueDate), true);
    }

    @Override
    public void deleteRecurringPayment(String id) {
        recurringPaymentRepository.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getVersions() {
        return versionRepository.findAll().stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("versionId", v.getVersionId());
            m.put("versionNumber", v.getVersionNumber());
            m.put("fileName", v.getFileName());
            m.put("uploadDate", v.getUploadDate());
            m.put("recordCount", v.getRecordCount());
            return m;
        }).collect(Collectors.toList());
    }

    private String resolveVersionId(String id) {
        if (id == null) return null;
        try {
            int num = Integer.parseInt(id);
            List<String> ids = jdbcTemplate.queryForList("SELECT version_id FROM dataset_version WHERE version_number = ? LIMIT 1", String.class, num);
            if (!ids.isEmpty()) return ids.get(0);
        } catch (NumberFormatException ignored) {}
        return id;
    }

    @Override
    public Map<String, Object> compareVersions(String v1, String v2) {
        String resolvedV1 = resolveVersionId(v1);
        String resolvedV2 = resolveVersionId(v2);

        List<Map<String, Object>> tx1 = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ?", resolvedV1);
        List<Map<String, Object>> tx2 = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ?", resolvedV2);

        Map<String, Map<String, Object>> map1 = new HashMap<>();
        for (Map<String, Object> t : tx1) {
            map1.put(String.valueOf(t.get("transaction_id")), t);
        }
        Map<String, Map<String, Object>> map2 = new HashMap<>();
        for (Map<String, Object> t : tx2) {
            map2.put(String.valueOf(t.get("transaction_id")), t);
        }

        List<Map<String, Object>> changes = new ArrayList<>();
        long added = 0;
        long modified = 0;
        long removed = 0;
        long unchanged = 0;

        for (Map.Entry<String, Map<String, Object>> entry : map2.entrySet()) {
            String tid = entry.getKey();
            Map<String, Object> t2 = entry.getValue();
            if (!map1.containsKey(tid)) {
                added++;
                Map<String, Object> c = new HashMap<>();
                c.put("transactionId", tid);
                c.put("changeType", "ADDED");
                c.put("fieldName", "Record");
                c.put("oldValue", "—");
                c.put("newValue", "₹" + t2.get("amount"));
                changes.add(c);
            } else {
                Map<String, Object> t1 = map1.get(tid);
                boolean isMod = false;
                if (t1.get("amount") != null && t2.get("amount") != null &&
                        new BigDecimal(t1.get("amount").toString()).compareTo(new BigDecimal(t2.get("amount").toString())) != 0) {
                    isMod = true;
                    Map<String, Object> c = new HashMap<>();
                    c.put("transactionId", tid);
                    c.put("changeType", "MODIFIED");
                    c.put("fieldName", "amount");
                    c.put("oldValue", "₹" + t1.get("amount"));
                    c.put("newValue", "₹" + t2.get("amount"));
                    changes.add(c);
                }
                if (!String.valueOf(t1.get("status")).equalsIgnoreCase(String.valueOf(t2.get("status")))) {
                    isMod = true;
                    Map<String, Object> c = new HashMap<>();
                    c.put("transactionId", tid);
                    c.put("changeType", "MODIFIED");
                    c.put("fieldName", "status");
                    c.put("oldValue", t1.get("status"));
                    c.put("newValue", t2.get("status"));
                    changes.add(c);
                }
                if (isMod) modified++;
                else unchanged++;
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : map1.entrySet()) {
            String tid = entry.getKey();
            if (!map2.containsKey(tid)) {
                removed++;
                Map<String, Object> c = new HashMap<>();
                c.put("transactionId", tid);
                c.put("changeType", "REMOVED");
                c.put("fieldName", "Record");
                c.put("oldValue", "₹" + entry.getValue().get("amount"));
                c.put("newValue", "—");
                changes.add(c);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("added", added);
        summary.put("modified", modified);
        summary.put("removed", removed);
        summary.put("unchanged", unchanged);

        Map<String, Object> res = new HashMap<>();
        res.put("changes", changes);
        res.put("summary", summary);
        res.put("changeSummary", summary);
        res.put("added", added);
        res.put("modified", modified);
        res.put("removed", removed);
        res.put("unchanged", unchanged);
        return res;
    }

    @Override
    public List<Map<String, Object>> getTimeline() {
        return jdbcTemplate.queryForList("SELECT * FROM dataset_version ORDER BY version_number ASC");
    }

    @Override
    public Map<String, Object> runScenarioSimulation(String name, String adjustmentType, BigDecimal amount, int delayDays) {
        String versionId = getLatestVersionId();
        if (versionId == null) return Collections.emptyMap();

        List<ForecastEntry> baseline = forecastService.generateForecast(versionId, 90);
        List<ForecastEntry> scenario = new ArrayList<>();
        if (baseline.isEmpty()) return Collections.emptyMap();

        BigDecimal rollingCash = baseline.get(0).getOpeningCash();
        BigDecimal shockAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : new BigDecimal("50000");
        int delay = Math.max(1, delayDays);

        for (int i = 0; i < baseline.size(); i++) {
            ForecastEntry b = baseline.get(i);
            ForecastEntry s = new ForecastEntry();
            s.setForecastDate(b.getForecastDate());
            s.setOpeningCash(rollingCash);

            BigDecimal inflow = b.getExpectedInflow();
            BigDecimal outflow = b.getExpectedOutflow();

            if ("DELAY_RECEIVABLES".equalsIgnoreCase(adjustmentType) || "DELAY_PAYMENT".equalsIgnoreCase(adjustmentType)) {
                if (i < delay) {
                    BigDecimal dailyReduction = shockAmount.divide(BigDecimal.valueOf(delay), 2, java.math.RoundingMode.HALF_UP);
                    inflow = inflow.subtract(dailyReduction).max(BigDecimal.ZERO);
                } else if (i == delay) {
                    inflow = inflow.add(shockAmount);
                }
            } else if ("INCREASE_EXPENSES".equalsIgnoreCase(adjustmentType)) {
                BigDecimal dailyExtra = shockAmount.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
                outflow = outflow.add(dailyExtra);
            } else if ("UNEXPECTED_EXPENSE".equalsIgnoreCase(adjustmentType)) {
                if (i == 0) {
                    outflow = outflow.add(shockAmount);
                }
            } else if ("REDUCE_EXPENSES".equalsIgnoreCase(adjustmentType)) {
                BigDecimal dailyCut = shockAmount.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
                outflow = outflow.subtract(dailyCut).max(BigDecimal.ZERO);
            } else if ("INCREASE_REVENUE".equalsIgnoreCase(adjustmentType)) {
                BigDecimal dailyBoost = shockAmount.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
                inflow = inflow.add(dailyBoost);
            }

            s.setExpectedInflow(inflow);
            s.setExpectedOutflow(outflow);
            BigDecimal closing = rollingCash.add(inflow).subtract(outflow);
            s.setClosingCash(closing);
            rollingCash = closing;

            scenario.add(s);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("baseline", baseline);
        res.put("scenario", scenario);
        res.put("name", name);
        return res;
    }

    @Override
    public CsvImportResult importCsv(File file) throws Exception {
        return csvImportService.importCsv(file);
    }

    @Override
    public Map<String, Object> getSettings() {
        ApplicationSettings s = settingsRepository.getSettings();
        if (s != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("theme", s.getTheme());
            m.put("forecastDays", s.getForecastDays());
            m.put("minimumCashThreshold", s.getMinimumCashThreshold());
            m.put("currency", s.getCurrency());
            m.put("shopkeeperPhone", s.getShopkeeperPhone());
            m.put("shopkeeperEmail", s.getShopkeeperEmail());
            return m;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("theme", "arctic-ledger");
        m.put("forecastDays", 90);
        m.put("minimumCashThreshold", new BigDecimal("50000"));
        m.put("currency", "INR");
        m.put("shopkeeperPhone", "+91 98765 00000");
        m.put("shopkeeperEmail", "shopkeeper@cashflow.com");
        return m;
    }

    @Override
    public void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency) {
        updateSettings(theme, forecastDays, minimumThreshold, currency, "+91 98765 00000", "shopkeeper@cashflow.com");
    }

    @Override
    public void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency, String shopkeeperPhone, String shopkeeperEmail) {
        ApplicationSettings s = new ApplicationSettings("DEFAULT", theme, forecastDays, minimumThreshold, currency, shopkeeperPhone, shopkeeperEmail);
        settingsRepository.update(s);
    }

    @Override
    public File exportReportCsv(File destination) throws Exception {
        String versionId = getLatestVersionId();
        if (versionId == null) throw new IllegalStateException("No dataset version available.");

        List<Transaction> list = transactionRepository.findAllByVersionId(versionId);
        try (FileWriter writer = new FileWriter(destination)) {
            writer.write("transaction_id,date,type,category,description,amount,customer,customer_phone,customer_email,supplier,due_date,status\n");
            for (Transaction t : list) {
                writer.write(String.format("%s,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s,%s,%s\n",
                        t.getTransactionId(),
                        t.getTransactionDate(),
                        t.getTransactionType(),
                        t.getCategory(),
                        t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "",
                        t.getAmount(),
                        t.getCustomer() != null ? t.getCustomer() : "",
                        t.getCustomerPhone() != null ? t.getCustomerPhone() : "",
                        t.getCustomerEmail() != null ? t.getCustomerEmail() : "",
                        t.getSupplier() != null ? t.getSupplier() : "",
                        t.getDueDate() != null ? t.getDueDate() : "",
                        t.getStatus()));
            }
        }
        return destination;
    }

    @Override
    public Map<String, Object> initiateCall(String transactionId, String notes) {
        return communicationService.initiateCall(transactionId, notes);
    }

    @Override
    public Map<String, Object> sendSmsNotice(String transactionId, String customMessage) {
        return communicationService.sendSmsNotice(transactionId, customMessage);
    }

    @Override
    public Map<String, Object> sendEmailNotice(String transactionId, String customSubject, String customBody) {
        return communicationService.sendEmailNotice(transactionId, customSubject, customBody);
    }

    @Override
    public Map<String, Object> notifyBothParties(String transactionId) {
        return communicationService.notifyBothParties(transactionId);
    }

    @Override
    public Map<String, Object> bulkNotifyDefaulters() {
        return communicationService.bulkNotifyDefaulters();
    }

    @Override
    public List<Map<String, Object>> getCommunicationLogs(String transactionId) {
        List<CommunicationLog> logs = communicationService.getCommunicationLogs(transactionId);
        return logs.stream().map(l -> {
            Map<String, Object> m = new HashMap<>();
            m.put("logId", l.getLogId());
            m.put("transactionId", l.getTransactionId());
            m.put("customerName", l.getCustomerName());
            m.put("channel", l.getChannel());
            m.put("recipientType", l.getRecipientType());
            m.put("recipientContact", l.getRecipientContact());
            m.put("subject", l.getSubject());
            m.put("message", l.getMessage());
            m.put("status", l.getStatus());
            m.put("timestamp", l.getTimestamp() != null ? l.getTimestamp().toString() : "");
            return m;
        }).collect(Collectors.toList());
    }
}
