package com.cashflow.service;

import com.cashflow.model.DataChange;
import com.cashflow.model.Transaction;
import com.cashflow.repository.DataChangeRepository;
import com.cashflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataComparisonServiceTest {

    private TransactionRepository transactionRepository;
    private DataChangeRepository dataChangeRepository;
    private DataComparisonService comparisonService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        dataChangeRepository = mock(DataChangeRepository.class);
        comparisonService = new DataComparisonService(transactionRepository, dataChangeRepository);
    }

    @Test
    void testDetectAddedModifiedAndRemovedTransactions() {
        // Old Version Transactions
        Transaction old1 = new Transaction();
        old1.setTransactionId("TX1");
        old1.setAmount(new BigDecimal("1000.00"));
        old1.setStatus("PENDING");

        Transaction old2 = new Transaction();
        old2.setTransactionId("TX2");
        old2.setAmount(new BigDecimal("2000.00"));
        old2.setStatus("PAID");

        when(transactionRepository.findAllByVersionId("v1")).thenReturn(new ArrayList<>(List.of(old1, old2)));

        // New Version Transactions:
        // TX1 modified (amount & status changed)
        // TX2 removed (omitted)
        // TX3 added
        Transaction new1 = new Transaction();
        new1.setTransactionId("TX1");
        new1.setAmount(new BigDecimal("1500.00"));
        new1.setStatus("PAID");

        Transaction new3 = new Transaction();
        new3.setTransactionId("TX3");
        new3.setAmount(new BigDecimal("3000.00"));
        new3.setStatus("PENDING");

        comparisonService.compareAndSaveChanges("v1", "v2", List.of(new1, new3));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DataChange>> captor = ArgumentCaptor.forClass(List.class);
        verify(dataChangeRepository).saveAll(captor.capture());

        List<DataChange> captured = captor.getValue();
        assertNotNull(captured);

        // Check ADDED for TX3
        assertTrue(captured.stream().anyMatch(c -> "TX3".equals(c.getTransactionId()) && "ADDED".equals(c.getChangeType())));

        // Check REMOVED for TX2
        assertTrue(captured.stream().anyMatch(c -> "TX2".equals(c.getTransactionId()) && "REMOVED".equals(c.getChangeType())));

        // Check MODIFIED for TX1 amount and status
        assertTrue(captured.stream().anyMatch(c -> "TX1".equals(c.getTransactionId()) && "MODIFIED".equals(c.getChangeType()) && "amount".equals(c.getFieldName())));
        assertTrue(captured.stream().anyMatch(c -> "TX1".equals(c.getTransactionId()) && "MODIFIED".equals(c.getChangeType()) && "status".equals(c.getFieldName())));
    }
}
