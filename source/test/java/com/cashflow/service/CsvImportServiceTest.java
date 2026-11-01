package com.cashflow.service;

import com.cashflow.model.Customer;
import com.cashflow.model.Transaction;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.repository.CustomerRepository;
import com.cashflow.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CsvImportServiceTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testImportSampleDataWithContacts() throws Exception {
        ClassPathResource resource = new ClassPathResource("sample-data.csv");
        File sampleFile = resource.getFile();

        CsvImportResult result = csvImportService.importCsv(sampleFile);
        assertNotNull(result);
        assertTrue(result.getValidRows() > 0);
        assertTrue(result.getErrors() == null || result.getErrors().isEmpty(), () -> "Errors: " + result.getErrors());

        // Verify transactions have phone & email parsed
        List<Transaction> txns = transactionRepository.findAllByVersionId(result.getVersionId());
        assertFalse(txns.isEmpty());

        Transaction overdueTxn = txns.stream()
                .filter(t -> "TXN006".equals(t.getTransactionId()))
                .findFirst()
                .orElse(null);

        assertNotNull(overdueTxn);
        assertEquals("PQR Industries", overdueTxn.getCustomer());
        assertEquals("+91-98765-10003", overdueTxn.getCustomerPhone());
        assertEquals("finance@pqrind.com", overdueTxn.getCustomerEmail());
        assertEquals("OVERDUE", overdueTxn.getStatus());

        // Verify customers table has phone & email populated
        List<Customer> customers = customerRepository.findAll();
        assertFalse(customers.isEmpty());
        Customer pqr = customers.stream()
                .filter(c -> "PQR Industries".equalsIgnoreCase(c.getCustomerName()))
                .findFirst()
                .orElse(null);

        assertNotNull(pqr);
        assertEquals("+91-98765-10003", pqr.getPhone());
        assertEquals("finance@pqrind.com", pqr.getEmail());
    }
}
