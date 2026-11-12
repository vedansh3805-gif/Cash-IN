package com.cashflow.controller;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.service.PenaltyService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receivables")
public class ReceivablesController {

    private final JdbcTemplate jdbcTemplate;
    private final PenaltyService penaltyService;

    public ReceivablesController(JdbcTemplate jdbcTemplate, PenaltyService penaltyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.penaltyService = penaltyService;
    }

    @GetMapping
    public Map<String, Object> getReceivables(@RequestParam(required = false) String versionId) {
        if (versionId == null) {
            versionId = jdbcTemplate.queryForObject("SELECT version_id FROM dataset_version ORDER BY version_number DESC LIMIT 1", String.class);
        }
        
        List<Map<String, Object>> receivables = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ? AND transaction_type = 'INCOME' AND status IN ('PENDING', 'OVERDUE') ORDER BY due_date ASC", versionId);
        
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        BigDecimal totalPenaltyAccrued = BigDecimal.ZERO;
        ApplicationSettings settings = penaltyService.getSettingsOrDefault();
        
        BigDecimal current = BigDecimal.ZERO;
        BigDecimal days30 = BigDecimal.ZERO;
        BigDecimal days60 = BigDecimal.ZERO;
        BigDecimal days90 = BigDecimal.ZERO;
        BigDecimal over90 = BigDecimal.ZERO;
        
        LocalDate today = LocalDate.now();
        LocalDate maxDate = null;
        for (Map<String, Object> r : receivables) {
            Object dueDateObj = r.get("due_date");
            if (dueDateObj != null) {
                try {
                    LocalDate d = LocalDate.parse(dueDateObj.toString());
                    if (maxDate == null || d.isAfter(maxDate)) maxDate = d;
                } catch (Exception ignored) {}
            }
        }
        LocalDate refDate = (maxDate != null && maxDate.isBefore(today.minusDays(30))) ? maxDate : today;

        for (Map<String, Object> r : receivables) {
            BigDecimal amt = new BigDecimal(r.get("amount").toString());
            totalOutstanding = totalOutstanding.add(amt);
            
            if ("OVERDUE".equalsIgnoreCase(String.valueOf(r.get("status")))) {
                overdueAmount = overdueAmount.add(amt);
            }
            
            Object dueDateObj = r.get("due_date");
            if (dueDateObj != null) {
                try {
                    LocalDate dueDate = LocalDate.parse(dueDateObj.toString());
                    long daysOverdue = ChronoUnit.DAYS.between(dueDate, refDate);
                    r.put("daysOverdue", Math.max(0, daysOverdue));
                    
                    if (daysOverdue <= 0) {
                        current = current.add(amt);
                    } else if (daysOverdue <= 30) {
                        days30 = days30.add(amt);
                    } else if (daysOverdue <= 60) {
                        days60 = days60.add(amt);
                    } else if (daysOverdue <= 90) {
                        days90 = days90.add(amt);
                    } else {
                        over90 = over90.add(amt);
                    }
                } catch (Exception e) {
                    current = current.add(amt);
                }
            } else {
                current = current.add(amt);
            }

            PenaltyService.PenaltyDetails pd = penaltyService.calculateFromMap(r, settings, refDate);
            r.put("penaltyAmount", pd.penaltyAmount());
            r.put("totalPayable", pd.totalPayable());
            r.put("penaltyWaived", pd.waived());
            r.put("billableDays", pd.billableDays());
            totalPenaltyAccrued = totalPenaltyAccrued.add(pd.penaltyAmount());
        }
        
        Map<String, BigDecimal> aging = new HashMap<>();
        aging.put("current", current);
        aging.put("days30", days30);
        aging.put("days60", days60);
        aging.put("days90", days90);
        aging.put("over90", over90);
        
        // Customer delays and contacts
        List<Map<String, Object>> customerDelays = jdbcTemplate.queryForList("SELECT customer_name as customerName, phone, email, average_payment_delay as averageDelay, total_outstanding as totalOutstanding, COALESCE(total_penalty, 0.0) as totalPenalty FROM customer ORDER BY average_payment_delay DESC");
        
        Double avgDelayTotal = jdbcTemplate.queryForObject("SELECT AVG(average_payment_delay) FROM customer WHERE average_payment_delay > 0", Double.class);
        
        Map<String, Object> response = new HashMap<>();
        response.put("receivables", receivables);
        response.put("totalOutstanding", totalOutstanding);
        response.put("overdueAmount", overdueAmount);
        response.put("totalPenaltyAccrued", totalPenaltyAccrued);
        response.put("totalCollectibleWithPenalty", totalOutstanding.add(totalPenaltyAccrued));
        response.put("averagePaymentDelay", avgDelayTotal != null ? avgDelayTotal : 0.0);
        response.put("aging", aging);
        response.put("customerDelays", customerDelays);
        response.put("penaltySettings", Map.of(
                "enabled", Boolean.TRUE.equals(settings.getPenaltyEnabled()),
                "type", settings.getPenaltyType(),
                "rate", settings.getPenaltyRate(),
                "gracePeriodDays", settings.getGracePeriodDays(),
                "maxPenaltyPercent", settings.getMaxPenaltyPercent()
        ));
        
        return response;
    }
}
