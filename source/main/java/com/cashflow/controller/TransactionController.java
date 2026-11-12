package com.cashflow.controller;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.service.PenaltyService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin("*")
public class TransactionController {

    private final JdbcTemplate jdbcTemplate;
    private final PenaltyService penaltyService;

    public TransactionController(JdbcTemplate jdbcTemplate, PenaltyService penaltyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.penaltyService = penaltyService;
    }

    @GetMapping
    public Map<String, Object> getTransactions(
            @RequestParam(required = false) String versionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transaction_date") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        StringBuilder sql = new StringBuilder("SELECT * FROM transaction_record WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM transaction_record WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (versionId != null && !versionId.isEmpty()) {
            sql.append(" AND version_id = ?");
            countSql.append(" AND version_id = ?");
            params.add(versionId);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND transaction_type = ?");
            countSql.append(" AND transaction_type = ?");
            params.add(type);
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            countSql.append(" AND category = ?");
            params.add(category);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            countSql.append(" AND status = ?");
            params.add(status);
        }
        if (search != null && !search.isEmpty()) {
            String like = "%" + search + "%";
            sql.append(" AND (description LIKE ? OR customer LIKE ? OR supplier LIKE ? OR customer_phone LIKE ? OR customer_email LIKE ?)");
            countSql.append(" AND (description LIKE ? OR customer LIKE ? OR supplier LIKE ? OR customer_phone LIKE ? OR customer_email LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        Integer totalRecords = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, params.toArray());
        int totalPages = (int) Math.ceil((double) (totalRecords == null ? 0 : totalRecords) / size);

        // Basic SQL injection protection for sort and dir
        String safeSort = sort.matches("[a-zA-Z0-9_]+") ? sort : "transaction_date";
        String safeDir = dir.equalsIgnoreCase("asc") ? "ASC" : "DESC";

        sql.append(" ORDER BY ").append(safeSort).append(" ").append(safeDir);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        List<Map<String, Object>> content = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        ApplicationSettings settings = penaltyService.getSettingsOrDefault();
        LocalDate today = LocalDate.now();
        for (Map<String, Object> row : content) {
            PenaltyService.PenaltyDetails pd = penaltyService.calculateFromMap(row, settings, today);
            row.put("daysLate", pd.daysLate());
            row.put("billableDays", pd.billableDays());
            row.put("penaltyAmount", pd.penaltyAmount());
            row.put("totalPayable", pd.totalPayable());
            row.put("penaltyWaived", pd.waived());
        }
        
        List<String> categories = jdbcTemplate.queryForList("SELECT DISTINCT category FROM transaction_record WHERE category IS NOT NULL", String.class);

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalPages", totalPages);
        response.put("number", page);
        response.put("categories", categories);
        
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getTransaction(@PathVariable String id) {
        List<Map<String, Object>> transactions;
        try {
            long numericId = Long.parseLong(id);
            transactions = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE id = ? OR transaction_id = ? ORDER BY id DESC LIMIT 1", numericId, id);
        } catch (NumberFormatException e) {
            transactions = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE transaction_id = ? ORDER BY id DESC LIMIT 1", id);
        }
        if (transactions.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> transaction = transactions.get(0);
        ApplicationSettings settings = penaltyService.getSettingsOrDefault();
        PenaltyService.PenaltyDetails pd = penaltyService.calculateFromMap(transaction, settings, LocalDate.now());
        transaction.put("daysLate", pd.daysLate());
        transaction.put("billableDays", pd.billableDays());
        transaction.put("penaltyAmount", pd.penaltyAmount());
        transaction.put("totalPayable", pd.totalPayable());
        transaction.put("penaltyWaived", pd.waived());

        String transactionId = (String) transaction.get("transaction_id");
        List<Map<String, Object>> history = jdbcTemplate.queryForList("SELECT * FROM data_change WHERE transaction_id = ? ORDER BY changed_at DESC", transactionId);

        Map<String, Object> response = new HashMap<>(transaction);
        response.put("transaction", transaction);
        response.put("history", history);
        response.put("changes", history);
        return response;
    }

    @PostMapping("/{id}/waive-penalty")
    public Map<String, Object> waivePenalty(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> tx = findTransactionRow(id);
        if (tx == null) {
            return Map.of("error", "Transaction not found");
        }
        boolean currentlyWaived = false;
        Object waivedObj = tx.get("penalty_waived");
        if (waivedObj instanceof Boolean b) currentlyWaived = b;
        else if (waivedObj instanceof Number n) currentlyWaived = n.intValue() != 0;
        else if (waivedObj != null) currentlyWaived = "true".equalsIgnoreCase(waivedObj.toString()) || "1".equals(waivedObj.toString());

        boolean newWaived = body != null && body.containsKey("waived")
                ? Boolean.parseBoolean(String.valueOf(body.get("waived")))
                : !currentlyWaived;

        long rowId = ((Number) tx.get("id")).longValue();
        tx.put("penalty_waived", newWaived);
        PenaltyService.PenaltyDetails pd = penaltyService.calculateFromMap(tx, penaltyService.getSettingsOrDefault(), LocalDate.now());

        jdbcTemplate.update("UPDATE transaction_record SET penalty_waived = ?, penalty_amount = ? WHERE id = ?",
                newWaived ? 1 : 0, pd.penaltyAmount(), rowId);

        return getTransaction(String.valueOf(rowId));
    }

    @PostMapping("/{id}/mark-paid")
    public Map<String, Object> markPaid(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> tx = findTransactionRow(id);
        if (tx == null) {
            return Map.of("error", "Transaction not found");
        }
        LocalDate paymentDate = LocalDate.now();
        if (body != null && body.get("paymentDate") != null && !body.get("paymentDate").toString().isBlank()) {
            paymentDate = LocalDate.parse(body.get("paymentDate").toString().substring(0, 10));
        }

        long rowId = ((Number) tx.get("id")).longValue();
        tx.put("status", "PAID");
        tx.put("payment_date", paymentDate.toString());
        PenaltyService.PenaltyDetails pd = penaltyService.calculateFromMap(tx, penaltyService.getSettingsOrDefault(), paymentDate);

        jdbcTemplate.update("UPDATE transaction_record SET status = 'PAID', payment_date = ?, penalty_amount = ? WHERE id = ?",
                paymentDate.toString(), pd.penaltyAmount(), rowId);

        return getTransaction(String.valueOf(rowId));
    }

    private Map<String, Object> findTransactionRow(String id) {
        List<Map<String, Object>> rows;
        try {
            long numericId = Long.parseLong(id);
            rows = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE id = ? OR transaction_id = ? ORDER BY id DESC LIMIT 1", numericId, id);
        } catch (NumberFormatException e) {
            rows = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE transaction_id = ? ORDER BY id DESC LIMIT 1", id);
        }
        return rows.isEmpty() ? null : rows.get(0);
    }
}
