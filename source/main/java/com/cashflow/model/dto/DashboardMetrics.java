package com.cashflow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {
    private BigDecimal currentCash;
    private BigDecimal expectedIncoming;
    private BigDecimal upcomingExpenses;
    private BigDecimal pendingReceivables;
    private BigDecimal overdueReceivables;
    private BigDecimal netCashFlow;
    private BigDecimal forecastedMinCash;
    private String riskStatus;
    private int pendingReceivablesCount;
    private int overdueCount;
    private int warningCount;
    private int forecastDays;
    private List<Map<String, Object>> cashFlowHistory;
    private List<Map<String, Object>> forecastSummary;
    private List<Map<String, Object>> upcomingPayments;
    private List<Map<String, Object>> warnings;
    private List<Map<String, Object>> recentChanges;
}
