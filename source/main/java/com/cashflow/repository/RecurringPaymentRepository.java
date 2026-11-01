package com.cashflow.repository;

import com.cashflow.model.RecurringPayment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class RecurringPaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    public RecurringPaymentRepository(JdbcTemplate jdbcTemplate) {
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

    private RowMapper<RecurringPayment> rowMapper = (rs, rowNum) -> {
        RecurringPayment r = new RecurringPayment();
        r.setRecurringId(rs.getString("recurring_id"));
        r.setDescription(rs.getString("description"));
        r.setCategory(rs.getString("category"));
        r.setAmount(rs.getBigDecimal("amount"));
        r.setFrequency(rs.getString("frequency"));
        r.setNextDueDate(parseDate(rs.getString("next_due_date")));
        r.setActive(rs.getBoolean("active"));
        return r;
    };

    public List<RecurringPayment> findAll() {
        return jdbcTemplate.query("SELECT * FROM recurring_payment", rowMapper);
    }
    
    public void save(RecurringPayment r) {
        String sql = "INSERT INTO recurring_payment (recurring_id, description, category, amount, frequency, next_due_date, active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, r.getRecurringId(), r.getDescription(), r.getCategory(), r.getAmount(), r.getFrequency(), r.getNextDueDate(), r.getActive());
    }
    
    public void update(RecurringPayment r) {
        String sql = "UPDATE recurring_payment SET description=?, category=?, amount=?, frequency=?, next_due_date=?, active=? WHERE recurring_id=?";
        jdbcTemplate.update(sql, r.getDescription(), r.getCategory(), r.getAmount(), r.getFrequency(), r.getNextDueDate(), r.getActive(), r.getRecurringId());
    }
    
    public void delete(String id) {
        jdbcTemplate.update("DELETE FROM recurring_payment WHERE recurring_id=?", id);
    }

    public void deleteById(String id) {
        delete(id);
    }
}
