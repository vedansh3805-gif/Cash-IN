package com.cashflow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvValidationServiceTest {

    private CsvValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new CsvValidationService();
    }

    @Test
    void testValidateValidCsv() {
        String csvContent = "transaction_id,date,type,category,description,amount,customer,supplier,due_date,status\n" +
                "TX001,2026-06-01,INCOME,Sales,Consulting Invoice,50000,Acme Corp,,2026-06-15,PAID\n" +
                "TX002,2026-06-02,EXPENSE,Rent,HQ Rent,20000,,Landlord,2026-06-05,PAID";

        MockMultipartFile file = new MockMultipartFile(
                "file", "valid.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<String> errors = validationService.validate(file);
        assertTrue(errors.isEmpty(), "Valid CSV should have 0 errors");
    }

    @Test
    void testValidateValidCsvWithPhoneAndEmail() {
        String csvContent = "transaction_id,date,type,category,description,amount,customer,customer_phone,customer_email,supplier,due_date,status\n" +
                "TX001,2026-06-01,INCOME,Sales,Consulting Invoice,50000,Acme Corp,+91-98765-10001,billing@acme.com,,2026-06-15,PAID\n" +
                "TX002,2026-06-02,EXPENSE,Rent,HQ Rent,20000,,,,Landlord,2026-06-05,PAID";

        MockMultipartFile file = new MockMultipartFile(
                "file", "valid_contacts.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<String> errors = validationService.validate(file);
        assertTrue(errors.isEmpty(), "Valid CSV with phone and email should have 0 errors");
    }

    @Test
    void testValidateInvalidPhoneAndEmailFormat() {
        String csvContent = "transaction_id,date,type,category,description,amount,customer,customer_phone,customer_email,supplier,due_date,status\n" +
                "TX001,2026-06-01,INCOME,Sales,Consulting Invoice,50000,Acme Corp,123,not-an-email,,2026-06-15,PAID";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid_contacts.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<String> errors = validationService.validate(file);
        assertFalse(errors.isEmpty(), "Invalid phone and email should produce errors");
        assertTrue(errors.stream().anyMatch(e -> e.contains("mobile number format")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("email format")));
    }

    @Test
    void testValidateInvalidHeaders() {
        String csvContent = "id,trans_date,amount\n" +
                "1,2026-06-01,500";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<String> errors = validationService.validate(file);
        assertFalse(errors.isEmpty(), "Invalid header should produce errors");
        assertTrue(errors.stream().anyMatch(e -> e.contains("Missing required header") || e.contains("header")),
                "Should notify about missing headers");
    }

    @Test
    void testValidateInvalidRowData() {
        String csvContent = "transaction_id,date,type,category,description,amount,customer,supplier,due_date,status\n" +
                ",invalid-date,UNKNOWN_TYPE,Sales,Missing ID,-5000,,,INVALID_STATUS";

        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid_row.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        List<String> errors = validationService.validate(file);
        assertFalse(errors.isEmpty(), "Invalid row fields should generate errors");
    }
}
