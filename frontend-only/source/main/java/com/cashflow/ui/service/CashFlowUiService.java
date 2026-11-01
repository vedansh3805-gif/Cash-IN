package com.cashflow.ui.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.model.dto.DashboardMetrics;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Common UI Service Interface.
 * Implemented by both:
 * 1. BackendUiService (in complete project, delegates to Spring services & SQLite)
 * 2. MockUiService (in frontend-only, provides mock data and zero-backend execution)
 */
public interface CashFlowUiService {

    DashboardMetrics getDashboardMetrics();

    List<Transaction> getTransactions(String search, String type, String category, String status);

    List<ForecastEntry> getForecast(int days);

    List<Warning> getWarnings();

    List<Map<String, Object>> getAnomalies();

    Map<String, Object> getReceivablesAnalysis();

    Map<String, Object> getExpenseAnalysis();

    List<Map<String, Object>> getRecurringPayments();

    void addRecurringPayment(String description, String category, BigDecimal amount, String frequency, String nextDueDate);

    void deleteRecurringPayment(String id);

    List<Map<String, Object>> getVersions();

    Map<String, Object> compareVersions(String v1, String v2);

    List<Map<String, Object>> getTimeline();

    Map<String, Object> runScenarioSimulation(String name, String adjustmentType, BigDecimal amount, int delayDays);

    CsvImportResult importCsv(File file) throws Exception;

    Map<String, Object> getSettings();

    void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency);

    void updateSettings(String theme, int forecastDays, BigDecimal minimumThreshold, String currency, String shopkeeperPhone, String shopkeeperEmail);

    File exportReportCsv(File destination) throws Exception;

    // Multi-channel Communication & Defaulter Outreach (Call, SMS, Email to Both Parties)
    Map<String, Object> initiateCall(String transactionId, String notes);

    Map<String, Object> sendSmsNotice(String transactionId, String customMessage);

    Map<String, Object> sendEmailNotice(String transactionId, String customSubject, String customBody);

    Map<String, Object> notifyBothParties(String transactionId);

    Map<String, Object> bulkNotifyDefaulters();

    List<Map<String, Object>> getCommunicationLogs(String transactionId);
}
