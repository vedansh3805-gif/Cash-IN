package com.cashflow.service;

import com.cashflow.model.dto.DashboardMetrics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CashFlowService {

    private final JdbcTemplate jdbcTemplate;
    private final ForecastService forecastService;
    private final WarningService warningService;
    private final AnomalyService anomalyService;

    public CashFlowService(JdbcTemplate jdbcTemplate, ForecastService forecastService, WarningService warningService, AnomalyService anomalyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.forecastService = forecastService;
        this.warningService = warningService;
        this.anomalyService = anomalyService;
    }

    public DashboardMetrics calculateMetrics(String versionId) {
        DashboardMetrics metrics = new DashboardMetrics();
        
        // Settings
        BigDecimal threshold = jdbcTemplate.queryForObject("SELECT minimum_cash_threshold FROM application_settings LIMIT 1", BigDecimal.class);
        if (threshold == null) threshold = new BigDecimal("50000");
        Integer forecastDays = jdbcTemplate.queryForObject("SELECT forecast_days FROM application_settings LIMIT 1", Integer.class);
        if (forecastDays == null) forecastDays = 90;
        metrics.setForecastDays(forecastDays);

        // Basic aggregations
        BigDecimal currentCash = getSum(versionId, "INCOME", "PAID").subtract(getSum(versionId, "EXPENSE", "PAID"));
        metrics.setCurrentCash(currentCash != null ? currentCash : BigDecimal.ZERO);
        
        BigDecimal expectedInflows = getSum(versionId, "INCOME", "PENDING").add(getSum(versionId, "INCOME", "OVERDUE"));
        metrics.setExpectedIncoming(expectedInflows);
        
        BigDecimal expectedOutflows = getSum(versionId, "EXPENSE", "PENDING").add(getSum(versionId, "EXPENSE", "OVERDUE"));
        metrics.setUpcomingExpenses(expectedOutflows);
        
        metrics.setNetCashFlow(expectedInflows.subtract(expectedOutflows));

        // Receivables
        BigDecimal pendingReceivables = getSum(versionId, "INCOME", "PENDING");
        metrics.setPendingReceivables(pendingReceivables);
        BigDecimal overdueReceivables = getSum(versionId, "INCOME", "OVERDUE");
        metrics.setOverdueReceivables(overdueReceivables);
        
        Integer pendingCount = getCount(versionId, "INCOME", "PENDING");
        metrics.setPendingReceivablesCount(pendingCount != null ? pendingCount : 0);
        Integer overdueCount = getCount(versionId, "INCOME", "OVERDUE");
        metrics.setOverdueCount(overdueCount != null ? overdueCount : 0);

        // Warnings
        Integer warningCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM warning WHERE version_id = ?", Integer.class, versionId);
        metrics.setWarningCount(warningCount != null ? warningCount : 0);
        
        List<Map<String, Object>> warnings = jdbcTemplate.queryForList("SELECT * FROM warning WHERE version_id = ? ORDER BY warning_date DESC LIMIT 5", versionId);
        metrics.setWarnings(warnings);

        // Forecast Summary
        List<Map<String, Object>> forecastSummary = jdbcTemplate.queryForList("SELECT * FROM forecast WHERE version_id = ? ORDER BY forecast_date ASC LIMIT 14", versionId);
        if (forecastSummary.isEmpty()) {
            try {
                forecastService.generateForecast(versionId, 90);
                forecastSummary = jdbcTemplate.queryForList("SELECT * FROM forecast WHERE version_id = ? ORDER BY forecast_date ASC LIMIT 14", versionId);
            } catch (Exception ignored) {}
        }
        for (Map<String, Object> f : forecastSummary) {
            f.put("date", f.get("forecast_date"));
            f.put("inflow", f.get("expected_inflow"));
            f.put("outflow", f.get("expected_outflow"));
            f.put("closing", f.get("closing_cash"));
        }
        metrics.setForecastSummary(forecastSummary);
        
        BigDecimal forecastedMinCash = jdbcTemplate.queryForObject("SELECT MIN(closing_cash) FROM forecast WHERE version_id = ?", BigDecimal.class, versionId);
        metrics.setForecastedMinCash(forecastedMinCash != null ? forecastedMinCash : BigDecimal.ZERO);
        
        if (metrics.getForecastedMinCash().compareTo(BigDecimal.ZERO) < 0) {
            metrics.setRiskStatus("CRITICAL");
        } else if (metrics.getForecastedMinCash().compareTo(threshold) < 0) {
            metrics.setRiskStatus("HIGH");
        } else if (metrics.getForecastedMinCash().compareTo(threshold.multiply(new BigDecimal("1.5"))) < 0) {
            metrics.setRiskStatus("MODERATE");
        } else {
            metrics.setRiskStatus("LOW");
        }

        // Cash flow history
        List<Map<String, Object>> rawHistory = jdbcTemplate.queryForList(
                "SELECT transaction_date, " +
                "SUM(CASE WHEN transaction_type='INCOME' THEN amount ELSE 0 END) as income, " +
                "SUM(CASE WHEN transaction_type='EXPENSE' THEN amount ELSE 0 END) as expense, " +
                "SUM(CASE WHEN transaction_type='INCOME' THEN amount ELSE -amount END) as net_amount " +
                "FROM transaction_record WHERE version_id = ? AND status = 'PAID' " +
                "GROUP BY transaction_date ORDER BY transaction_date ASC", versionId);

        BigDecimal runningBalance = BigDecimal.ZERO;
        List<Map<String, Object>> history = new java.util.ArrayList<>();
        for (Map<String, Object> h : rawHistory) {
            Map<String, Object> item = new HashMap<>(h);
            BigDecimal net = new BigDecimal(h.get("net_amount").toString());
            runningBalance = runningBalance.add(net);
            item.put("date", h.get("transaction_date"));
            item.put("balance", runningBalance);
            history.add(item);
        }
        metrics.setCashFlowHistory(history);

        // Upcoming payments
        List<Map<String, Object>> upcoming = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ? AND transaction_type = 'EXPENSE' AND status IN ('PENDING', 'OVERDUE') ORDER BY due_date ASC LIMIT 5", versionId);
        metrics.setUpcomingPayments(upcoming);

        // Recent changes
        List<Map<String, Object>> changes = jdbcTemplate.queryForList("SELECT * FROM data_change WHERE version_id = ? ORDER BY changed_at DESC LIMIT 5", versionId);
        metrics.setRecentChanges(changes);

        return metrics;
    }

    private BigDecimal getSum(String versionId, String type, String status) {
        BigDecimal sum = jdbcTemplate.queryForObject("SELECT SUM(amount) FROM transaction_record WHERE version_id = ? AND transaction_type = ? AND status = ?", BigDecimal.class, versionId, type, status);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private Integer getCount(String versionId, String type, String status) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_record WHERE version_id = ? AND transaction_type = ? AND status = ?", Integer.class, versionId, type, status);
    }
}
