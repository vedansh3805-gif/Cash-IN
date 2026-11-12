package com.cashflow.service;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyServiceTest {

    private final PenaltyService penaltyService = new PenaltyService(null);

    private ApplicationSettings defaultSettings() {
        ApplicationSettings s = new ApplicationSettings(
                "DEFAULT", "arctic-ledger", 90,
                new BigDecimal("50000"), "INR",
                "+91 98765 00000", "shopkeeper@cashflow.com"
        );
        s.setPenaltyEnabled(true);
        s.setPenaltyType("DAILY_PERCENT");
        s.setPenaltyRate(new BigDecimal("0.1"));
        s.setGracePeriodDays(3);
        s.setMaxPenaltyPercent(new BigDecimal("25.0"));
        return s;
    }

    @Test
    void unpaidOverdueInvoiceAccruesDailyPenaltyAfterGracePeriod() {
        Transaction t = new Transaction();
        t.setTransactionType("INCOME");
        t.setStatus("OVERDUE");
        t.setAmount(new BigDecimal("10000.00"));
        t.setDueDate(LocalDate.of(2026, 10, 1));

        // 13 days late, 3 grace days => 10 billable days * 0.1% * 10000 = 100.00
        PenaltyService.PenaltyDetails pd = penaltyService.calculate(t, defaultSettings(), LocalDate.of(2026, 10, 14));
        assertEquals(13, pd.daysLate());
        assertEquals(10, pd.billableDays());
        assertEquals(new BigDecimal("100.00"), pd.penaltyAmount());
        assertEquals(new BigDecimal("10100.00"), pd.totalPayable());
    }

    @Test
    void paidLateInvoiceCalculatesPenaltyFromDueDateToPaymentDate() {
        Transaction t = new Transaction();
        t.setTransactionType("INCOME");
        t.setStatus("PAID");
        t.setAmount(new BigDecimal("20000.00"));
        t.setDueDate(LocalDate.of(2026, 10, 1));
        t.setPaymentDate(LocalDate.of(2026, 10, 9)); // 8 days late, 5 billable days

        // 5 * 0.1% * 20000 = 100.00
        PenaltyService.PenaltyDetails pd = penaltyService.calculate(t, defaultSettings(), LocalDate.of(2026, 11, 12));
        assertEquals(8, pd.daysLate());
        assertEquals(5, pd.billableDays());
        assertEquals(new BigDecimal("100.00"), pd.penaltyAmount());
        assertEquals(new BigDecimal("20100.00"), pd.totalPayable());
    }

    @Test
    void paidOnTimeOrWithinGracePeriodHasZeroPenalty() {
        Transaction onTime = new Transaction();
        onTime.setTransactionType("INCOME");
        onTime.setStatus("PAID");
        onTime.setAmount(new BigDecimal("15000.00"));
        onTime.setDueDate(LocalDate.of(2026, 10, 10));
        onTime.setPaymentDate(LocalDate.of(2026, 10, 10));
        assertEquals(new BigDecimal("0.00"), penaltyService.calculate(onTime, defaultSettings(), LocalDate.now()).penaltyAmount());

        Transaction withinGrace = new Transaction();
        withinGrace.setTransactionType("INCOME");
        withinGrace.setStatus("OVERDUE");
        withinGrace.setAmount(new BigDecimal("15000.00"));
        withinGrace.setDueDate(LocalDate.of(2026, 10, 10));
        assertEquals(new BigDecimal("0.00"), penaltyService.calculate(withinGrace, defaultSettings(), LocalDate.of(2026, 10, 13)).penaltyAmount());
    }

    @Test
    void penaltyRespectsMaxCapAndFlatAndPercentageModes() {
        Transaction t = new Transaction();
        t.setTransactionType("INCOME");
        t.setStatus("OVERDUE");
        t.setAmount(new BigDecimal("1000.00"));
        t.setDueDate(LocalDate.of(2025, 1, 1));

        // Very old invoice capped at 25% of 1000 = 250.00
        PenaltyService.PenaltyDetails capped = penaltyService.calculate(t, defaultSettings(), LocalDate.of(2026, 10, 1));
        assertEquals(new BigDecimal("250.00"), capped.penaltyAmount());

        ApplicationSettings flat = defaultSettings();
        flat.setPenaltyType("FLAT");
        flat.setPenaltyRate(new BigDecimal("150.00"));
        assertEquals(new BigDecimal("150.00"), penaltyService.calculate(t, flat, LocalDate.of(2025, 1, 10)).penaltyAmount());

        ApplicationSettings pct = defaultSettings();
        pct.setPenaltyType("PERCENTAGE");
        pct.setPenaltyRate(new BigDecimal("5.0"));
        assertEquals(new BigDecimal("50.00"), penaltyService.calculate(t, pct, LocalDate.of(2025, 1, 10)).penaltyAmount());
    }

    @Test
    void waivedOrDisabledPenaltyReturnsZero() {
        Transaction t = new Transaction();
        t.setTransactionType("INCOME");
        t.setStatus("OVERDUE");
        t.setAmount(new BigDecimal("10000.00"));
        t.setDueDate(LocalDate.of(2026, 10, 1));
        t.setPenaltyWaived(true);

        assertEquals(new BigDecimal("0.00"), penaltyService.calculate(t, defaultSettings(), LocalDate.of(2026, 10, 20)).penaltyAmount());

        t.setPenaltyWaived(false);
        ApplicationSettings disabled = defaultSettings();
        disabled.setPenaltyEnabled(false);
        assertEquals(new BigDecimal("0.00"), penaltyService.calculate(t, disabled, LocalDate.of(2026, 10, 20)).penaltyAmount());
    }
}
