package com.cashflow.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.RecurringPayment;
import com.cashflow.model.Transaction;
import com.cashflow.repository.ForecastRepository;
import com.cashflow.repository.RecurringPaymentRepository;
import com.cashflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ForecastServiceTest {

    private TransactionRepository transactionRepository;
    private RecurringPaymentRepository recurringPaymentRepository;
    private ForecastRepository forecastRepository;
    private ForecastService forecastService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        recurringPaymentRepository = mock(RecurringPaymentRepository.class);
        forecastRepository = mock(ForecastRepository.class);
        forecastService = new ForecastService(transactionRepository, recurringPaymentRepository, forecastRepository);
    }

    @Test
    void testGenerateForecastCalculation() {
        String versionId = "v1";

        // Initial realized cash: Paid Income (100000) - Paid Expense (40000) = 60000
        Transaction paidIncome = new Transaction();
        paidIncome.setTransactionType("INCOME");
        paidIncome.setStatus("PAID");
        paidIncome.setAmount(new BigDecimal("100000.00"));

        Transaction paidExpense = new Transaction();
        paidExpense.setTransactionType("EXPENSE");
        paidExpense.setStatus("PAID");
        paidExpense.setAmount(new BigDecimal("40000.00"));

        // Pending income due tomorrow: +25000
        Transaction pendingIncome = new Transaction();
        pendingIncome.setTransactionType("INCOME");
        pendingIncome.setStatus("PENDING");
        pendingIncome.setAmount(new BigDecimal("25000.00"));
        pendingIncome.setDueDate(LocalDate.now().plusDays(1));

        when(transactionRepository.findAllByVersionId(versionId))
                .thenReturn(List.of(paidIncome, paidExpense, pendingIncome));

        // Active recurring expense due day after tomorrow: -10000
        RecurringPayment recurring = new RecurringPayment();
        recurring.setActive(true);
        recurring.setAmount(new BigDecimal("10000.00"));
        recurring.setNextDueDate(LocalDate.now().plusDays(2));

        when(recurringPaymentRepository.findAll()).thenReturn(List.of(recurring));

        List<ForecastEntry> forecast = forecastService.generateForecast(versionId, 7);

        assertNotNull(forecast);
        assertEquals(7, forecast.size(), "Forecast should generate 7 daily entries");

        // Day 0: opening = 60000, inflow = 0, outflow = 0, closing = 60000
        assertEquals(new BigDecimal("60000.00"), forecast.get(0).getOpeningCash());
        assertEquals(new BigDecimal("60000.00"), forecast.get(0).getClosingCash());

        // Day 1: opening = 60000, inflow = 25000, outflow = 0, closing = 85000
        assertEquals(new BigDecimal("60000.00"), forecast.get(1).getOpeningCash());
        assertEquals(new BigDecimal("25000.00"), forecast.get(1).getExpectedInflow());
        assertEquals(new BigDecimal("85000.00"), forecast.get(1).getClosingCash());

        // Day 2: opening = 85000, inflow = 0, outflow = 10000, closing = 75000
        assertEquals(new BigDecimal("85000.00"), forecast.get(2).getOpeningCash());
        assertEquals(new BigDecimal("10000.00"), forecast.get(2).getExpectedOutflow());
        assertEquals(new BigDecimal("75000.00"), forecast.get(2).getClosingCash());

        verify(forecastRepository).saveAll(any());
    }
}
