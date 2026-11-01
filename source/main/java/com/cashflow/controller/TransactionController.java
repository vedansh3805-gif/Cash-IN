package com.cashflow.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
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

    public TransactionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        String transactionId = (String) transaction.get("transaction_id");
        List<Map<String, Object>> history = jdbcTemplate.queryForList("SELECT * FROM data_change WHERE transaction_id = ? ORDER BY changed_at DESC", transactionId);

        Map<String, Object> response = new HashMap<>(transaction);
        response.put("transaction", transaction);
        response.put("history", history);
        response.put("changes", history);
        return response;
    }
}
