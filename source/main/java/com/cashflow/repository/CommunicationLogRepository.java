package com.cashflow.repository;

import com.cashflow.model.CommunicationLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CommunicationLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommunicationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CommunicationLog> rowMapper = (rs, rowNum) -> {
        CommunicationLog log = new CommunicationLog();
        log.setLogId(rs.getString("log_id"));
        log.setTransactionId(rs.getString("transaction_id"));
        log.setCustomerName(rs.getString("customer_name"));
        log.setChannel(rs.getString("channel"));
        log.setRecipientType(rs.getString("recipient_type"));
        log.setRecipientContact(rs.getString("recipient_contact"));
        log.setSubject(rs.getString("subject"));
        log.setMessage(rs.getString("message"));
        log.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            log.setTimestamp(ts.toLocalDateTime());
        }
        return log;
    };

    public void save(CommunicationLog log) {
        String sql = "INSERT INTO communication_log (log_id, transaction_id, customer_name, channel, recipient_type, recipient_contact, subject, message, status, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                log.getLogId(),
                log.getTransactionId(),
                log.getCustomerName(),
                log.getChannel(),
                log.getRecipientType(),
                log.getRecipientContact(),
                log.getSubject(),
                log.getMessage(),
                log.getStatus(),
                log.getTimestamp() != null ? Timestamp.valueOf(log.getTimestamp()) : Timestamp.valueOf(LocalDateTime.now())
        );
    }

    public List<CommunicationLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM communication_log ORDER BY timestamp DESC", rowMapper);
    }

    public List<CommunicationLog> findByTransactionId(String transactionId) {
        return jdbcTemplate.query("SELECT * FROM communication_log WHERE transaction_id = ? ORDER BY timestamp DESC", rowMapper, transactionId);
    }

    public List<CommunicationLog> findByCustomerName(String customerName) {
        return jdbcTemplate.query("SELECT * FROM communication_log WHERE customer_name = ? ORDER BY timestamp DESC", rowMapper, customerName);
    }
}
