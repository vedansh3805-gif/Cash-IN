package com.cashflow.service;

import com.cashflow.model.CommunicationLog;
import com.cashflow.model.Transaction;
import com.cashflow.repository.CommunicationLogRepository;
import com.cashflow.repository.CustomerRepository;
import com.cashflow.repository.SettingsRepository;
import com.cashflow.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommunicationServiceTest {

    private TransactionRepository transactionRepository;
    private CustomerRepository customerRepository;
    private SettingsRepository settingsRepository;
    private CommunicationLogRepository logRepository;
    private CommunicationService communicationService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        customerRepository = mock(CustomerRepository.class);
        settingsRepository = mock(SettingsRepository.class);
        logRepository = mock(CommunicationLogRepository.class);

        communicationService = new CommunicationService(
                transactionRepository,
                customerRepository,
                settingsRepository,
                logRepository
        );
    }

    private Transaction createOverdueTransaction() {
        Transaction t = new Transaction();
        t.setTransactionId("TXN-TEST-100");
        t.setTransactionDate(LocalDate.now().minusDays(10));
        t.setTransactionType("INCOME");
        t.setCategory("Sales");
        t.setDescription("Overdue Invoice Test");
        t.setAmount(new BigDecimal("75000.00"));
        t.setCustomer("Acme Buyers");
        t.setCustomerPhone("+91-98765-44444");
        t.setCustomerEmail("finance@acmebuyers.com");
        t.setDueDate(LocalDate.now().minusDays(5));
        t.setStatus("OVERDUE");
        return t;
    }

    @Test
    void testInitiateCallLogsToBothParties() {
        Transaction t = createOverdueTransaction();
        when(transactionRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Optional.of(t));

        Map<String, Object> result = communicationService.initiateCall("TXN-TEST-100", "Discussed payment extension");

        assertTrue((Boolean) result.get("success"));
        assertEquals("CALL", result.get("channel"));
        assertEquals("+91-98765-44444", result.get("buyerPhone"));
        assertNotNull(result.get("shopkeeperPhone"));

        // Verify logs created for BOTH buyer and shopkeeper
        ArgumentCaptor<CommunicationLog> captor = ArgumentCaptor.forClass(CommunicationLog.class);
        verify(logRepository, times(2)).save(captor.capture());

        List<CommunicationLog> savedLogs = captor.getAllValues();
        assertEquals(2, savedLogs.size());
        assertTrue(savedLogs.stream().anyMatch(l -> "BUYER".equals(l.getRecipientType())));
        assertTrue(savedLogs.stream().anyMatch(l -> "SHOPKEEPER".equals(l.getRecipientType())));
    }

    @Test
    void testSendSmsNoticeToBothParties() {
        Transaction t = createOverdueTransaction();
        when(transactionRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Optional.of(t));

        Map<String, Object> result = communicationService.sendSmsNotice("TXN-TEST-100", null);

        assertTrue((Boolean) result.get("success"));
        assertEquals("SMS", result.get("channel"));
        assertEquals("SENT", result.get("status"));
        assertTrue(result.get("buyerMessage").toString().contains("TXN-TEST-100"));
        assertTrue(result.get("shopkeeperMessage").toString().contains("AUDIT"));

        verify(logRepository, times(2)).save(any(CommunicationLog.class));
    }

    @Test
    void testSendEmailNoticeToBothParties() {
        Transaction t = createOverdueTransaction();
        when(transactionRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Optional.of(t));

        Map<String, Object> result = communicationService.sendEmailNotice("TXN-TEST-100", null, null);

        assertTrue((Boolean) result.get("success"));
        assertEquals("EMAIL", result.get("channel"));
        assertEquals("finance@acmebuyers.com", result.get("buyerEmail"));
        assertNotNull(result.get("shopkeeperEmail"));

        verify(logRepository, times(2)).save(any(CommunicationLog.class));
    }

    @Test
    void testNotifyBothPartiesConsolidated() {
        Transaction t = createOverdueTransaction();
        when(transactionRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Optional.of(t));

        Map<String, Object> result = communicationService.notifyBothParties("TXN-TEST-100");

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("sms"));
        assertNotNull(result.get("email"));

        // 2 SMS logs (buyer + shopkeeper) + 2 Email logs (buyer + shopkeeper) = 4 total logs
        verify(logRepository, times(4)).save(any(CommunicationLog.class));
    }

    @Test
    void testGetCommunicationLogs() {
        when(logRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Collections.emptyList());
        List<CommunicationLog> logs = communicationService.getCommunicationLogs("TXN-TEST-100");
        assertNotNull(logs);
        verify(logRepository).findByTransactionId("TXN-TEST-100");
    }

    @Test
    void testBulkNotifyDefaulters() {
        Transaction t1 = createOverdueTransaction();
        Transaction t2 = new Transaction();
        t2.setTransactionId("TXN-TEST-101");
        t2.setTransactionDate(LocalDate.now().minusDays(5));
        t2.setTransactionType("INCOME");
        t2.setCategory("Sales");
        t2.setAmount(new BigDecimal("30000.00"));
        t2.setCustomer("Beta Corp");
        t2.setCustomerPhone("+91-98765-55555");
        t2.setCustomerEmail("accounts@betacorp.com");
        t2.setDueDate(LocalDate.now().minusDays(2));
        t2.setStatus("OVERDUE");

        Transaction tPaid = new Transaction();
        tPaid.setTransactionId("TXN-TEST-PAID");
        tPaid.setTransactionType("INCOME");
        tPaid.setStatus("PAID");

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2, tPaid));
        when(transactionRepository.findByTransactionId("TXN-TEST-100")).thenReturn(Optional.of(t1));
        when(transactionRepository.findByTransactionId("TXN-TEST-101")).thenReturn(Optional.of(t2));

        Map<String, Object> result = communicationService.bulkNotifyDefaulters();

        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("notifiedCount"));
        assertEquals(2, result.get("totalOverdue"));
        verify(logRepository, times(8)).save(any(CommunicationLog.class));
    }
}
