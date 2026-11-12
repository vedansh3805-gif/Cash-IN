package com.cashflow.repository;

import com.cashflow.model.Transaction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            if (raw.length() > 10) raw = raw.substring(0, 10);
            return LocalDate.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private RowMapper<Transaction> rowMapper = (rs, rowNum) -> {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setTransactionId(rs.getString("transaction_id"));
        t.setVersionId(rs.getString("version_id"));
        t.setTransactionDate(parseDate(rs.getString("transaction_date")));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setCategory(rs.getString("category"));
        t.setDescription(rs.getString("description"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setCustomer(rs.getString("customer"));
        try { t.setCustomerPhone(rs.getString("customer_phone")); } catch (Exception ignored) {}
        try { t.setCustomerEmail(rs.getString("customer_email")); } catch (Exception ignored) {}
        t.setSupplier(rs.getString("supplier"));
        t.setDueDate(parseDate(rs.getString("due_date")));
        t.setPaymentDate(parseDate(rs.getString("payment_date")));
        t.setStatus(rs.getString("status"));
        try {
            java.math.BigDecimal p = rs.getBigDecimal("penalty_amount");
            t.setPenaltyAmount(p != null ? p : java.math.BigDecimal.ZERO);
        } catch (Exception ignored) { t.setPenaltyAmount(java.math.BigDecimal.ZERO); }
        try {
            t.setPenaltyWaived(rs.getObject("penalty_waived") != null && rs.getBoolean("penalty_waived"));
        } catch (Exception ignored) { t.setPenaltyWaived(false); }
        return t;
    };

    public void save(Transaction t) {
        String sql = "INSERT INTO transaction_record (transaction_id, version_id, transaction_date, transaction_type, category, description, amount, customer, customer_phone, customer_email, supplier, due_date, payment_date, status, penalty_amount, penalty_waived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, t.getTransactionId(), t.getVersionId(), t.getTransactionDate(), t.getTransactionType(), t.getCategory(), t.getDescription(), t.getAmount(), t.getCustomer(), t.getCustomerPhone(), t.getCustomerEmail(), t.getSupplier(), t.getDueDate(), t.getPaymentDate(), t.getStatus(),
                t.getPenaltyAmount() != null ? t.getPenaltyAmount() : java.math.BigDecimal.ZERO,
                Boolean.TRUE.equals(t.getPenaltyWaived()));
    }

    public void saveAll(List<Transaction> transactions) {
        String sql = "INSERT INTO transaction_record (transaction_id, version_id, transaction_date, transaction_type, category, description, amount, customer, customer_phone, customer_email, supplier, due_date, payment_date, status, penalty_amount, penalty_waived) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, transactions, 100, (ps, t) -> {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, t.getVersionId());
            ps.setObject(3, t.getTransactionDate());
            ps.setString(4, t.getTransactionType());
            ps.setString(5, t.getCategory());
            ps.setString(6, t.getDescription());
            ps.setBigDecimal(7, t.getAmount());
            ps.setString(8, t.getCustomer());
            ps.setString(9, t.getCustomerPhone());
            ps.setString(10, t.getCustomerEmail());
            ps.setString(11, t.getSupplier());
            ps.setObject(12, t.getDueDate());
            ps.setObject(13, t.getPaymentDate());
            ps.setString(14, t.getStatus());
            ps.setBigDecimal(15, t.getPenaltyAmount() != null ? t.getPenaltyAmount() : java.math.BigDecimal.ZERO);
            ps.setBoolean(16, Boolean.TRUE.equals(t.getPenaltyWaived()));
        });
    }

    public List<Transaction> findAllByVersionId(String versionId) {
        return jdbcTemplate.query("SELECT * FROM transaction_record WHERE version_id = ?", rowMapper, versionId);
    }
    
    public Optional<Transaction> findById(Long id) {
        List<Transaction> list = jdbcTemplate.query("SELECT * FROM transaction_record WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Transaction> findByTransactionId(String transactionId) {
        List<Transaction> list = jdbcTemplate.query("SELECT * FROM transaction_record WHERE transaction_id = ? ORDER BY id DESC LIMIT 1", rowMapper, transactionId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
    
    public List<Transaction> findAll() {
        return jdbcTemplate.query("SELECT * FROM transaction_record", rowMapper);
    }
}
