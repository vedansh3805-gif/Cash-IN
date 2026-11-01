package com.cashflow.service;

import com.cashflow.model.Anomaly;
import com.cashflow.model.Transaction;
import com.cashflow.repository.AnomalyRepository;
import com.cashflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnomalyServiceTest {

    private AnomalyRepository anomalyRepository;
    private TransactionRepository transactionRepository;
    private AnomalyService anomalyService;

    @BeforeEach
    void setUp() {
        anomalyRepository = mock(AnomalyRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        anomalyService = new AnomalyService(anomalyRepository, transactionRepository);
    }

    @Test
    void testDetectOutlierExpenseAnomaly() {
        String versionId = "v1";

        // Normal expenses: 10000, 12000, 8000 -> avg around 10000
        // Outlier expense: 60000 (> 3x average)
        Transaction t1 = new Transaction();
        t1.setTransactionId("TX1");
        t1.setTransactionType("EXPENSE");
        t1.setAmount(new BigDecimal("10000.00"));

        Transaction t2 = new Transaction();
        t2.setTransactionId("TX2");
        t2.setTransactionType("EXPENSE");
        t2.setAmount(new BigDecimal("12000.00"));

        Transaction t3 = new Transaction();
        t3.setTransactionId("TX3");
        t3.setTransactionType("EXPENSE");
        t3.setAmount(new BigDecimal("8000.00"));

        Transaction t4 = new Transaction();
        t4.setTransactionId("TX4");
        t4.setTransactionType("EXPENSE");
        t4.setAmount(new BigDecimal("150000.00")); // Massive spike

        when(transactionRepository.findAllByVersionId(versionId)).thenReturn(List.of(t1, t2, t3, t4));

        anomalyService.detectAnomalies(versionId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Anomaly>> captor = ArgumentCaptor.forClass(List.class);
        verify(anomalyRepository).saveAll(captor.capture());

        List<Anomaly> anomalies = captor.getValue();
        assertNotNull(anomalies);
        assertEquals(1, anomalies.size());
        assertEquals("TX4", anomalies.get(0).getTransactionId());
        assertEquals("LARGE_EXPENSE", anomalies.get(0).getAnomalyType());
    }
}
