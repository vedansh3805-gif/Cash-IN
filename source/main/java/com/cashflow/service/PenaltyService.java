package com.cashflow.service;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.model.Transaction;
import com.cashflow.repository.SettingsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Computes late and unpaid payment penalties for overdue or late-settled income transactions.
 */
@Service
public class PenaltyService {

    private final SettingsRepository settingsRepository;

    public PenaltyService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public record PenaltyDetails(
            long daysLate,
            long billableDays,
            BigDecimal penaltyAmount,
            BigDecimal totalPayable,
            boolean waived
    ) {}

    public ApplicationSettings getSettingsOrDefault() {
        ApplicationSettings settings = settingsRepository != null ? settingsRepository.getSettings() : null;
        if (settings == null) {
            settings = new ApplicationSettings(
                    "DEFAULT", "arctic-ledger", 90,
                    new BigDecimal("50000"), "INR",
                    "+91 98765 00000", "shopkeeper@cashflow.com"
            );
        }
        if (settings.getPenaltyEnabled() == null) settings.setPenaltyEnabled(true);
        if (settings.getPenaltyType() == null || settings.getPenaltyType().isBlank()) {
            settings.setPenaltyType("DAILY_PERCENT");
        }
        if (settings.getPenaltyRate() == null) settings.setPenaltyRate(new BigDecimal("0.1"));
        if (settings.getGracePeriodDays() == null) settings.setGracePeriodDays(3);
        if (settings.getMaxPenaltyPercent() == null) settings.setMaxPenaltyPercent(new BigDecimal("25.0"));
        return settings;
    }

    public PenaltyDetails calculate(Transaction t, ApplicationSettings settings, LocalDate refDate) {
        if (t == null) {
            return new PenaltyDetails(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, false);
        }
        return calculate(
                t.getAmount(),
                t.getTransactionType(),
                t.getStatus(),
                t.getDueDate(),
                t.getPaymentDate(),
                Boolean.TRUE.equals(t.getPenaltyWaived()),
                settings,
                refDate
        );
    }

    public PenaltyDetails calculateFromMap(Map<String, Object> row, ApplicationSettings settings, LocalDate refDate) {
        BigDecimal amount = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
        String type = row.get("transaction_type") != null ? row.get("transaction_type").toString() : "";
        String status = row.get("status") != null ? row.get("status").toString() : "";
        LocalDate dueDate = parseDate(row.get("due_date"));
        LocalDate paymentDate = parseDate(row.get("payment_date"));
        boolean waived = false;
        Object waivedObj = row.get("penalty_waived");
        if (waivedObj instanceof Boolean b) {
            waived = b;
        } else if (waivedObj instanceof Number n) {
            waived = n.intValue() != 0;
        } else if (waivedObj != null) {
            waived = "true".equalsIgnoreCase(waivedObj.toString()) || "1".equals(waivedObj.toString());
        }
        return calculate(amount, type, status, dueDate, paymentDate, waived, settings, refDate);
    }

    public PenaltyDetails calculate(
            BigDecimal amount,
            String transactionType,
            String status,
            LocalDate dueDate,
            LocalDate paymentDate,
            boolean waived,
            ApplicationSettings settings,
            LocalDate refDate
    ) {
        BigDecimal principal = amount != null ? amount : BigDecimal.ZERO;
        if (!"INCOME".equalsIgnoreCase(transactionType) || dueDate == null || "CANCELLED".equalsIgnoreCase(status)) {
            return new PenaltyDetails(0, 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), principal, waived);
        }

        long daysLate = 0;
        if ("PAID".equalsIgnoreCase(status)) {
            if (paymentDate != null && paymentDate.isAfter(dueDate)) {
                daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate);
            }
        } else if ("PENDING".equalsIgnoreCase(status) || "OVERDUE".equalsIgnoreCase(status)) {
            LocalDate effectiveEnd = refDate != null ? refDate : LocalDate.now();
            if (effectiveEnd.isAfter(dueDate)) {
                daysLate = ChronoUnit.DAYS.between(dueDate, effectiveEnd);
            }
        }

        if (settings == null) {
            settings = getSettingsOrDefault();
        }
        if (Boolean.FALSE.equals(settings.getPenaltyEnabled()) || waived || daysLate <= 0) {
            return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), principal, waived);
        }

        int graceDays = settings.getGracePeriodDays() != null ? Math.max(0, settings.getGracePeriodDays()) : 3;
        long billableDays = Math.max(0, daysLate - graceDays);
        if (billableDays <= 0) {
            return new PenaltyDetails(daysLate, 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), principal, waived);
        }

        BigDecimal rate = settings.getPenaltyRate() != null ? settings.getPenaltyRate() : new BigDecimal("0.1");
        String penaltyType = settings.getPenaltyType() != null ? settings.getPenaltyType().toUpperCase() : "DAILY_PERCENT";

        BigDecimal penalty;
        switch (penaltyType) {
            case "FLAT" -> penalty = rate;
            case "PERCENTAGE" -> penalty = principal.multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            case "DAILY_PERCENT" -> penalty = principal.multiply(rate)
                    .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(billableDays));
            case "DAILY_FLAT" -> penalty = rate.multiply(BigDecimal.valueOf(billableDays));
            default -> penalty = principal.multiply(rate)
                    .divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(billableDays));
        }

        BigDecimal maxCapPct = settings.getMaxPenaltyPercent() != null ? settings.getMaxPenaltyPercent() : new BigDecimal("25.0");
        if (maxCapPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxAllowed = principal.multiply(maxCapPct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (penalty.compareTo(maxAllowed) > 0) {
                penalty = maxAllowed;
            }
        }

        penalty = penalty.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPayable = principal.add(penalty).setScale(2, RoundingMode.HALF_UP);
        return new PenaltyDetails(daysLate, billableDays, penalty, totalPayable, false);
    }

    private static LocalDate parseDate(Object raw) {
        if (raw == null) return null;
        String s = raw.toString().trim();
        if (s.isEmpty()) return null;
        try {
            if (s.length() > 10) s = s.substring(0, 10);
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
