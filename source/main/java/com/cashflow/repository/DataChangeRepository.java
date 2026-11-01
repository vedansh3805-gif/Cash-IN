package com.cashflow.repository;

import com.cashflow.model.DataChange;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class DataChangeRepository {
    private final JdbcTemplate jdbcTemplate;

    public DataChangeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw.replace(" ", "T"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw).atStartOfDay();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private final RowMapper<DataChange> rowMapper = (rs, rowNum) -> {
        DataChange c = new DataChange();
        c.setChangeId(rs.getString("change_id"));
        c.setVersionId(rs.getString("version_id"));
        c.setTransactionId(rs.getString("transaction_id"));
        c.setChangeType(rs.getString("change_type"));
        c.setFieldName(rs.getString("field_name"));
        c.setOldValue(rs.getString("old_value"));
        c.setNewValue(rs.getString("new_value"));
        c.setChangedAt(parseDateTime(rs.getString("changed_at")));
        return c;
    };

    public void saveAll(List<DataChange> changes) {
        String sql = "INSERT INTO data_change (change_id, version_id, transaction_id, change_type, field_name, old_value, new_value, changed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, changes, 100, (ps, c) -> {
            ps.setString(1, c.getChangeId());
            ps.setString(2, c.getVersionId());
            ps.setString(3, c.getTransactionId());
            ps.setString(4, c.getChangeType());
            ps.setString(5, c.getFieldName());
            ps.setString(6, c.getOldValue());
            ps.setString(7, c.getNewValue());
            String formattedDate = c.getChangedAt() != null ? c.getChangedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
            ps.setString(8, formattedDate);
        });
    }

    public List<DataChange> findByVersionId(String versionId) {
        return jdbcTemplate.query("SELECT * FROM data_change WHERE version_id = ?", rowMapper, versionId);
    }
}
