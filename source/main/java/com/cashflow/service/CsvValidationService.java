package com.cashflow.service;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class CsvValidationService {

    private static final List<String> REQUIRED_FIELDS = List.of(
            "transaction_id", "date", "type", "category", "description",
            "amount", "customer", "supplier", "due_date", "status"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\-\\s()]{7,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public List<String> validate(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        if (file == null || file.isEmpty()) {
            errors.add("File is empty.");
            return errors;
        }

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            if (rows.isEmpty()) {
                errors.add("CSV file contains no data.");
                return errors;
            }

            String[] header = rows.get(0);
            errors.addAll(validateHeaders(header));
            if (!errors.isEmpty()) {
                return errors;
            }

            Map<String, Integer> colMap = getHeaderColumnMap(header);
            for (int i = 1; i < rows.size(); i++) {
                errors.addAll(validateRow(rows.get(i), i + 1, colMap));
            }

        } catch (Exception e) {
            errors.add("Corrupted or unreadable CSV file: " + e.getMessage());
        }

        return errors;
    }

    public List<String> validateHeaders(String[] header) {
        List<String> errors = new ArrayList<>();
        if (header == null || header.length < REQUIRED_FIELDS.size()) {
            errors.add("Missing required headers. Expected at least 10 columns: " + String.join(",", REQUIRED_FIELDS));
            return errors;
        }

        Map<String, Integer> colMap = getHeaderColumnMap(header);
        for (String field : REQUIRED_FIELDS) {
            if (!colMap.containsKey(field)) {
                errors.add("Missing required header column: '" + field + "'");
            }
        }

        return errors;
    }

    public List<String> validateRow(String[] row, int rowIndex) {
        // Fallback for calls passing row without header map
        return validateRow(row, rowIndex, null);
    }

    public List<String> validateRow(String[] row, int rowIndex, Map<String, Integer> colMap) {
        List<String> errors = new ArrayList<>();
        if (row == null || row.length < 10) {
            errors.add("Row " + rowIndex + ": Insufficient columns.");
            return errors;
        }

        int idIdx = getColIndex(colMap, "transaction_id", 0);
        int dateIdx = getColIndex(colMap, "date", 1);
        int typeIdx = getColIndex(colMap, "type", 2);
        int amountIdx = getColIndex(colMap, "amount", 5);
        int statusIdx = getColIndex(colMap, "status", 9);
        int phoneIdx = getColIndex(colMap, "customer_phone", -1);
        int emailIdx = getColIndex(colMap, "customer_email", -1);

        String transactionId = getVal(row, idIdx);
        if (transactionId == null || transactionId.trim().isEmpty()) {
            errors.add("Row " + rowIndex + ": Transaction ID is required.");
        }

        String dateVal = getVal(row, dateIdx);
        try {
            LocalDate.parse(dateVal != null ? dateVal.trim() : "", DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException | NullPointerException e) {
            errors.add("Row " + rowIndex + ": Invalid date format. Expected YYYY-MM-DD.");
        }

        String type = getVal(row, typeIdx);
        type = type != null ? type.trim().toUpperCase() : "";
        if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
            errors.add("Row " + rowIndex + ": Type must be INCOME or EXPENSE.");
        }

        String amountVal = getVal(row, amountIdx);
        try {
            BigDecimal amount = new BigDecimal(amountVal != null ? amountVal.trim() : "");
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Row " + rowIndex + ": Amount cannot be negative.");
            }
        } catch (NumberFormatException | NullPointerException e) {
            errors.add("Row " + rowIndex + ": Invalid amount format.");
        }

        String status = getVal(row, statusIdx);
        status = status != null ? status.trim().toUpperCase() : "";
        if (!status.equals("PAID") && !status.equals("PENDING") && !status.equals("OVERDUE") && !status.equals("CANCELLED")) {
            errors.add("Row " + rowIndex + ": Status must be PAID, PENDING, OVERDUE, or CANCELLED.");
        }

        // Validate customer phone if provided
        if (phoneIdx != -1) {
            String phone = getVal(row, phoneIdx);
            if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
                errors.add("Row " + rowIndex + ": Invalid mobile number format for customer: '" + phone.trim() + "'. Expected 7-20 digits with optional +.");
            }
        }

        // Validate customer email if provided
        if (emailIdx != -1) {
            String email = getVal(row, emailIdx);
            if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
                errors.add("Row " + rowIndex + ": Invalid email format for customer: '" + email.trim() + "'.");
            }
        }

        return errors;
    }

    public static Map<String, Integer> getHeaderColumnMap(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;

        for (int i = 0; i < header.length; i++) {
            if (header[i] == null) continue;
            String h = header[i].trim().toLowerCase().replace(" ", "_");

            if (h.equals("transaction_id") || h.equals("id") || h.equals("tx_id")) {
                map.put("transaction_id", i);
            } else if (h.equals("date") || h.equals("transaction_date") || h.equals("tx_date")) {
                map.put("date", i);
            } else if (h.equals("type") || h.equals("transaction_type") || h.equals("tx_type")) {
                map.put("type", i);
            } else if (h.equals("category")) {
                map.put("category", i);
            } else if (h.equals("description") || h.equals("desc")) {
                map.put("description", i);
            } else if (h.equals("amount")) {
                map.put("amount", i);
            } else if (h.equals("customer") || h.equals("buyer") || h.equals("client")) {
                map.put("customer", i);
            } else if (h.equals("customer_phone") || h.equals("phone") || h.equals("mobile") || h.equals("buyer_phone") || h.equals("customer_mobile")) {
                map.put("customer_phone", i);
            } else if (h.equals("customer_email") || h.equals("email") || h.equals("buyer_email")) {
                map.put("customer_email", i);
            } else if (h.equals("supplier") || h.equals("vendor")) {
                map.put("supplier", i);
            } else if (h.equals("due_date")) {
                map.put("due_date", i);
            } else if (h.equals("payment_date")) {
                map.put("payment_date", i);
            } else if (h.equals("status")) {
                map.put("status", i);
            }
        }

        // Positional fallback for legacy 10/11 columns if names didn't map
        if (!map.containsKey("transaction_id") && header.length > 0) map.put("transaction_id", 0);
        if (!map.containsKey("date") && header.length > 1) map.put("date", 1);
        if (!map.containsKey("type") && header.length > 2) map.put("type", 2);
        if (!map.containsKey("category") && header.length > 3) map.put("category", 3);
        if (!map.containsKey("description") && header.length > 4) map.put("description", 4);
        if (!map.containsKey("amount") && header.length > 5) map.put("amount", 5);
        if (!map.containsKey("customer") && header.length > 6) map.put("customer", 6);
        if (!map.containsKey("supplier") && header.length > 7 && !map.containsKey("customer_phone")) map.put("supplier", 7);
        if (!map.containsKey("due_date") && header.length > 8) map.put("due_date", 8);
        if (!map.containsKey("status") && header.length > 9) map.put("status", 9);

        return map;
    }

    private static int getColIndex(Map<String, Integer> map, String key, int defaultIdx) {
        if (map != null && map.containsKey(key)) {
            return map.get(key);
        }
        return defaultIdx;
    }

    private static String getVal(String[] row, int idx) {
        if (row != null && idx >= 0 && idx < row.length) {
            return row[idx];
        }
        return null;
    }
}
