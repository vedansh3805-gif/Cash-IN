package com.cashflow.service;

import com.cashflow.model.DataChange;
import com.cashflow.model.DatasetVersion;
import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.repository.DataChangeRepository;
import com.cashflow.repository.DatasetVersionRepository;
import com.cashflow.repository.TransactionRepository;
import com.opencsv.CSVReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvImportService {

    private final CsvValidationService validationService;
    private final TransactionRepository transactionRepository;
    private final DatasetVersionRepository versionRepository;
    private final DataComparisonService comparisonService;
    private final DataChangeRepository dataChangeRepository;
    private final ForecastService forecastService;
    private final WarningService warningService;
    private final AnomalyService anomalyService;
    private final ReceivablesService receivablesService;
    private final JdbcTemplate jdbcTemplate;

    public CsvImportService(CsvValidationService validationService,
                            TransactionRepository transactionRepository,
                            DatasetVersionRepository versionRepository,
                            DataComparisonService comparisonService,
                            DataChangeRepository dataChangeRepository,
                            ForecastService forecastService,
                            WarningService warningService,
                            AnomalyService anomalyService,
                            ReceivablesService receivablesService,
                            JdbcTemplate jdbcTemplate) {
        this.validationService = validationService;
        this.transactionRepository = transactionRepository;
        this.versionRepository = versionRepository;
        this.comparisonService = comparisonService;
        this.dataChangeRepository = dataChangeRepository;
        this.forecastService = forecastService;
        this.warningService = warningService;
        this.anomalyService = anomalyService;
        this.receivablesService = receivablesService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public CsvImportResult importCsv(File file) throws Exception {
        try (Reader reader = new java.io.FileReader(file)) {
            return processCsvReader(reader, file.getName());
        }
    }

    @Transactional
    public CsvImportResult importCsv(MultipartFile file) throws Exception {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            return processCsvReader(reader, file.getOriginalFilename());
        }
    }

    private CsvImportResult processCsvReader(Reader reader, String fileName) throws Exception {
        List<String> errors = new ArrayList<>();
        List<Transaction> validTransactions = new ArrayList<>();
        int invalidRows = 0;
        int validRows = 0;

        String versionId = UUID.randomUUID().toString();

        try (CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                errors.add("File is empty.");
                return new CsvImportResult(0, 0, errors, null);
            }

            // Validate Header
            String[] header = rows.get(0);
            List<String> headerErrors = validationService.validateHeaders(header);
            if (!headerErrors.isEmpty()) {
                errors.addAll(headerErrors);
                return new CsvImportResult(0, rows.size(), errors, null);
            }

            Map<String, Integer> colMap = CsvValidationService.getHeaderColumnMap(header);

            // Validate and parse Rows
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                List<String> rowErrors = validationService.validateRow(row, i + 1, colMap);
                if (!rowErrors.isEmpty()) {
                    errors.addAll(rowErrors);
                    invalidRows++;
                } else {
                    Transaction t = new Transaction();
                    t.setVersionId(versionId);

                    t.setTransactionId(getVal(row, colMap.getOrDefault("transaction_id", 0)));
                    t.setTransactionDate(LocalDate.parse(getVal(row, colMap.getOrDefault("date", 1)), DateTimeFormatter.ISO_LOCAL_DATE));
                    t.setTransactionType(getVal(row, colMap.getOrDefault("type", 2)).toUpperCase());
                    t.setCategory(getVal(row, colMap.getOrDefault("category", 3)));
                    t.setDescription(getVal(row, colMap.getOrDefault("description", 4)));
                    t.setAmount(new BigDecimal(getVal(row, colMap.getOrDefault("amount", 5))));

                    String cust = getVal(row, colMap.getOrDefault("customer", 6));
                    t.setCustomer(cust != null && !cust.isBlank() ? cust : null);

                    if (colMap.containsKey("customer_phone")) {
                        String phone = getVal(row, colMap.get("customer_phone"));
                        t.setCustomerPhone(phone != null && !phone.isBlank() ? phone : null);
                    }
                    if (colMap.containsKey("customer_email")) {
                        String email = getVal(row, colMap.get("customer_email"));
                        t.setCustomerEmail(email != null && !email.isBlank() ? email : null);
                    }

                    int suppIdx = colMap.getOrDefault("supplier", -1);
                    if (suppIdx != -1) {
                        String supp = getVal(row, suppIdx);
                        t.setSupplier(supp != null && !supp.isBlank() ? supp : null);
                    }

                    int dueIdx = colMap.getOrDefault("due_date", -1);
                    if (dueIdx != -1) {
                        String due = getVal(row, dueIdx);
                        t.setDueDate(due != null && !due.isBlank() ? LocalDate.parse(due, DateTimeFormatter.ISO_LOCAL_DATE) : null);
                    }

                    int statusIdx = colMap.getOrDefault("status", -1);
                    if (statusIdx != -1) {
                        String status = getVal(row, statusIdx);
                        t.setStatus(status != null ? status.toUpperCase() : "PENDING");
                    } else {
                        t.setStatus("PENDING");
                    }

                    int payIdx = colMap.getOrDefault("payment_date", -1);
                    if (payIdx != -1) {
                        String pay = getVal(row, payIdx);
                        if (pay != null && !pay.isBlank()) {
                            t.setPaymentDate(LocalDate.parse(pay, DateTimeFormatter.ISO_LOCAL_DATE));
                        }
                    }

                    validTransactions.add(t);
                    validRows++;
                }
            }
        }

        int versionNum = 1;
        int added = 0;
        int modified = 0;
        int removed = 0;
        int unchanged = validRows;

        if (validRows > 0) {
            Optional<DatasetVersion> prevVersionOpt = versionRepository.findLatest();
            String prevVersionId = prevVersionOpt.map(DatasetVersion::getVersionId).orElse(null);
            versionNum = prevVersionOpt.map(v -> v.getVersionNumber() + 1).orElse(1);

            DatasetVersion version = new DatasetVersion(
                    versionId,
                    versionNum,
                    fileName,
                    LocalDateTime.now(),
                    validRows,
                    prevVersionId
            );
            versionRepository.save(version);
            transactionRepository.saveAll(validTransactions);

            // 1. Version Comparison & Delta Tracking
            if (prevVersionId != null) {
                comparisonService.compareAndSaveChanges(prevVersionId, versionId, validTransactions);
                List<DataChange> changes = dataChangeRepository.findByVersionId(versionId);
                added = (int) changes.stream().filter(c -> "ADDED".equals(c.getChangeType())).count();
                modified = (int) changes.stream().filter(c -> "MODIFIED".equals(c.getChangeType())).count();
                removed = (int) changes.stream().filter(c -> "REMOVED".equals(c.getChangeType())).count();
                unchanged = Math.max(0, validRows - added - modified);
            } else {
                added = validRows;
                unchanged = 0;
            }

            // 2. Load Threshold & Forecast Horizon from Application Settings
            BigDecimal threshold = new BigDecimal("50000");
            int forecastDays = 90;
            try {
                BigDecimal dbThreshold = jdbcTemplate.queryForObject("SELECT minimum_cash_threshold FROM application_settings LIMIT 1", BigDecimal.class);
                if (dbThreshold != null) threshold = dbThreshold;
                Integer dbDays = jdbcTemplate.queryForObject("SELECT forecast_days FROM application_settings LIMIT 1", Integer.class);
                if (dbDays != null) forecastDays = dbDays;
            } catch (Exception ignored) {}

            // 3. Compute Forecast
            List<ForecastEntry> forecast = forecastService.generateForecast(versionId, forecastDays);

            // 4. Generate Warnings
            warningService.generateWarnings(versionId, forecast, threshold);

            // 5. Detect Anomalies
            anomalyService.detectAnomalies(versionId);

            // 6. Update Customer Metrics & Outstanding Receivables
            receivablesService.updateCustomerMetrics(versionId);
        }

        CsvImportResult result = new CsvImportResult();
        result.setTotalRows(validRows + invalidRows);
        result.setValidRows(validRows);
        result.setInvalidRows(invalidRows);
        result.setAdded(added);
        result.setModified(modified);
        result.setRemoved(removed);
        result.setUnchanged(unchanged);
        result.setVersionNumber(versionNum);
        result.setVersionId(versionId);
        result.setErrors(errors);

        return result;
    }

    private String getVal(String[] row, int idx) {
        if (row != null && idx >= 0 && idx < row.length) {
            String val = row[idx];
            return val != null ? val.trim() : null;
        }
        return null;
    }
}
