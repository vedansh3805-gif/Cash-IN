package com.cashflow.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.repository.TransactionRepository;
import com.cashflow.repository.WarningRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarningServiceTest {

    private WarningRepository warningRepository;
    private TransactionRepository transactionRepository;
    private WarningService warningService;

    @BeforeEach
    void setUp() {
        warningRepository = mock(WarningRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        warningService = new WarningService(warningRepository, transactionRepository);
    }

    @Test
    void testDetectLowCashThresholdBreach() {
        String versionId = "v1";
        BigDecimal threshold = new BigDecimal("50000.00");

        ForecastEntry normalDay = new ForecastEntry();
        normalDay.setForecastDate(LocalDate.now());
        normalDay.setClosingCash(new BigDecimal("70000.00"));

        ForecastEntry breachDay = new ForecastEntry();
        breachDay.setForecastDate(LocalDate.now().plusDays(5));
        breachDay.setClosingCash(new BigDecimal("35000.00"));

        when(transactionRepository.findAllByVersionId(versionId)).thenReturn(List.of());

        warningService.generateWarnings(versionId, List.of(normalDay, breachDay), threshold);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Warning>> captor = ArgumentCaptor.forClass(List.class);
        verify(warningRepository).saveAll(captor.capture());

        List<Warning> warnings = captor.getValue();
        assertNotNull(warnings);
        assertTrue(warnings.stream().anyMatch(w -> "LOW_PROJECTED_CASH".equals(w.getWarningType()) &&
                w.getMessage().contains("Projected cash falls below threshold")));
    }

    @Test
    void testDetectOverdueReceivablesAndLargeExpense() {
        String versionId = "v1";

        Transaction overdueInvoice = new Transaction();
        overdueInvoice.setTransactionType("INCOME");
        overdueInvoice.setStatus("OVERDUE");
        overdueInvoice.setAmount(new BigDecimal("120000.00"));
        overdueInvoice.setCustomer("ABC Tech");

        Transaction largeExpense = new Transaction();
        largeExpense.setTransactionType("EXPENSE");
        largeExpense.setStatus("PENDING");
        largeExpense.setAmount(new BigDecimal("80000.00"));
        largeExpense.setCategory("Salary");
        largeExpense.setDescription("Staff Payroll");
        largeExpense.setDueDate(LocalDate.now().plusDays(3));

        when(transactionRepository.findAllByVersionId(versionId)).thenReturn(List.of(overdueInvoice, largeExpense));

        warningService.generateWarnings(versionId, List.of(), new BigDecimal("50000.00"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Warning>> captor = ArgumentCaptor.forClass(List.class);
        verify(warningRepository).saveAll(captor.capture());

        List<Warning> warnings = captor.getValue();
        assertTrue(warnings.stream().anyMatch(w -> "OVERDUE_RECEIVABLE".equals(w.getWarningType())));
        assertTrue(warnings.stream().anyMatch(w -> "LARGE_UPCOMING_EXPENSE".equals(w.getWarningType())));
    }
}
