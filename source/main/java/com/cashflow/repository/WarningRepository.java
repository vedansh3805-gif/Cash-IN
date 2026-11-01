package com.cashflow.repository;

import com.cashflow.model.Warning;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class WarningRepository {
    private final JdbcTemplate jdbcTemplate;

    public WarningRepository(JdbcTemplate jdbcTemplate) {
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

    private RowMapper<Warning> rowMapper = (rs, rowNum) -> {
        Warning w = new Warning();
        w.setWarningId(rs.getString("warning_id"));
        w.setVersionId(rs.getString("version_id"));
        w.setWarningType(rs.getString("warning_type"));
        w.setSeverity(rs.getString("severity"));
        w.setWarningDate(parseDate(rs.getString("warning_date")));
        w.setProjectedDate(parseDate(rs.getString("projected_date")));
        w.setMessage(rs.getString("message"));
        w.setStatus(rs.getString("status"));
        return w;
    };

    public void saveAll(List<Warning> warnings) {
        String sql = "INSERT INTO warning (warning_id, version_id, warning_type, severity, warning_date, projected_date, message, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, warnings, 100, (ps, w) -> {
            ps.setString(1, w.getWarningId());
            ps.setString(2, w.getVersionId());
            ps.setString(3, w.getWarningType());
            ps.setString(4, w.getSeverity());
            ps.setObject(5, w.getWarningDate());
            ps.setObject(6, w.getProjectedDate());
            ps.setString(7, w.getMessage());
            ps.setString(8, w.getStatus());
        });
    }

    public List<Warning> findByVersionId(String versionId) {
        return jdbcTemplate.query("SELECT * FROM warning WHERE version_id = ? ORDER BY warning_date DESC", rowMapper, versionId);
    }
}
