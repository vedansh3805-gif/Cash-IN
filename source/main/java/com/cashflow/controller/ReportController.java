package com.cashflow.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    private final JdbcTemplate jdbcTemplate;

    public ReportController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> getReport(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        String versionId = jdbcTemplate.queryForObject("SELECT version_id FROM dataset_version ORDER BY version_number DESC LIMIT 1", String.class);
        Integer versionNumber = jdbcTemplate.queryForObject("SELECT version_number FROM dataset_version WHERE version_id = ?", Integer.class, versionId);

        String fromDate = from != null ? from : LocalDate.now().minusDays(30).toString();
        String toDate = to != null ? to : LocalDate.now().toString();

        BigDecimal currentCash = getSum(versionId, "INCOME", "PAID", null, null).subtract(getSum(versionId, "EXPENSE", "PAID", null, null));
        BigDecimal totalIncome = getSum(versionId, "INCOME", "PAID", fromDate, toDate);
        BigDecimal totalExpenses = getSum(versionId, "EXPENSE", "PAID", fromDate, toDate);
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        BigDecimal totalReceivables = getSum(versionId, "INCOME", "PENDING", null, null);
        BigDecimal overdueAmount = getSum(versionId, "INCOME", "OVERDUE", null, null);
        Integer overdueCount = getCount(versionId, "INCOME", "OVERDUE", null, null);

        Integer forecastDays = jdbcTemplate.queryForObject("SELECT forecast_days FROM application_settings LIMIT 1", Integer.class);
        if (forecastDays == null) forecastDays = 90;

        BigDecimal forecastedMinCash = jdbcTemplate.queryForObject("SELECT MIN(closing_cash) FROM forecast WHERE version_id = ?", BigDecimal.class, versionId);
        if (forecastedMinCash == null) forecastedMinCash = BigDecimal.ZERO;

        BigDecimal forecastedEndCash = jdbcTemplate.queryForObject("SELECT closing_cash FROM forecast WHERE version_id = ? ORDER BY forecast_date DESC LIMIT 1", BigDecimal.class, versionId);
        if (forecastedEndCash == null) forecastedEndCash = BigDecimal.ZERO;

        Integer warningCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM warning WHERE version_id = ?", Integer.class, versionId);
        Integer anomalyCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM anomaly WHERE version_id = ?", Integer.class, versionId);

        List<Map<String, Object>> warnings = jdbcTemplate.queryForList("SELECT * FROM warning WHERE version_id = ?", versionId);
        List<Map<String, Object>> anomalies = jdbcTemplate.queryForList("SELECT * FROM anomaly WHERE version_id = ?", versionId);

        Map<String, Object> report = new HashMap<>();
        report.put("dateFrom", fromDate);
        report.put("dateTo", toDate);
        report.put("versionNumber", versionNumber);
        report.put("currentCash", currentCash);
        report.put("totalIncome", totalIncome);
        report.put("totalExpenses", totalExpenses);
        report.put("netCashFlow", netCashFlow);
        report.put("totalReceivables", totalReceivables);
        report.put("overdueAmount", overdueAmount);
        report.put("overdueCount", overdueCount != null ? overdueCount : 0);
        report.put("forecastDays", forecastDays);
        report.put("forecastedMinCash", forecastedMinCash);
        report.put("forecastedEndCash", forecastedEndCash);
        report.put("warningCount", warningCount != null ? warningCount : 0);
        report.put("anomalyCount", anomalyCount != null ? anomalyCount : 0);
        report.put("warnings", warnings);
        report.put("anomalies", anomalies);

        return report;
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportReportCsv(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        Map<String, Object> report = getReport(from, to);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append("Date From,").append(report.get("dateFrom")).append("\n");
        csv.append("Date To,").append(report.get("dateTo")).append("\n");
        csv.append("Current Cash,").append(report.get("currentCash")).append("\n");
        csv.append("Total Income,").append(report.get("totalIncome")).append("\n");
        csv.append("Total Expenses,").append(report.get("totalExpenses")).append("\n");
        csv.append("Net Cash Flow,").append(report.get("netCashFlow")).append("\n");
        csv.append("Total Receivables,").append(report.get("totalReceivables")).append("\n");
        csv.append("Overdue Amount,").append(report.get("overdueAmount")).append("\n");
        csv.append("Forecast Days,").append(report.get("forecastDays")).append("\n");

        byte[] bytes = csv.toString().getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    private BigDecimal getSum(String versionId, String type, String status, String fromDate, String toDate) {
        String sql = "SELECT SUM(amount) FROM transaction_record WHERE version_id = ? AND transaction_type = ? AND status = ?";
        List<Object> params = new java.util.ArrayList<>(java.util.Arrays.asList(versionId, type, status));
        
        if (fromDate != null && toDate != null) {
            sql += " AND transaction_date >= ? AND transaction_date <= ?";
            params.add(fromDate);
            params.add(toDate);
        }
        
        BigDecimal sum = jdbcTemplate.queryForObject(sql, BigDecimal.class, params.toArray());
        return sum != null ? sum : BigDecimal.ZERO;
    }

    private Integer getCount(String versionId, String type, String status, String fromDate, String toDate) {
        String sql = "SELECT COUNT(*) FROM transaction_record WHERE version_id = ? AND transaction_type = ? AND status = ?";
        List<Object> params = new java.util.ArrayList<>(java.util.Arrays.asList(versionId, type, status));
        
        if (fromDate != null && toDate != null) {
            sql += " AND transaction_date >= ? AND transaction_date <= ?";
            params.add(fromDate);
            params.add(toDate);
        }
        
        return jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
    }
}
