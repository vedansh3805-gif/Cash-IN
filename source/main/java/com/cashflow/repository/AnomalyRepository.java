package com.cashflow.repository;

import com.cashflow.model.Anomaly;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class AnomalyRepository {
    private final JdbcTemplate jdbcTemplate;

    public AnomalyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Anomaly> rowMapper = (rs, rowNum) -> {
        Anomaly a = new Anomaly();
        a.setAnomalyId(rs.getString("anomaly_id"));
        a.setVersionId(rs.getString("version_id"));
        a.setTransactionId(rs.getString("transaction_id"));
        a.setAnomalyType(rs.getString("anomaly_type"));
        a.setSeverity(rs.getString("severity"));
        a.setExpectedValue(rs.getBigDecimal("expected_value"));
        a.setActualValue(rs.getBigDecimal("actual_value"));
        a.setExplanation(rs.getString("explanation"));
        return a;
    };

    public void saveAll(List<Anomaly> anomalies) {
        String sql = "INSERT INTO anomaly (anomaly_id, version_id, transaction_id, anomaly_type, severity, expected_value, actual_value, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, anomalies, 100, (ps, a) -> {
            ps.setString(1, a.getAnomalyId());
            ps.setString(2, a.getVersionId());
            ps.setString(3, a.getTransactionId());
            ps.setString(4, a.getAnomalyType());
            ps.setString(5, a.getSeverity());
            ps.setBigDecimal(6, a.getExpectedValue());
            ps.setBigDecimal(7, a.getActualValue());
            ps.setString(8, a.getExplanation());
        });
    }

    public List<Anomaly> findByVersionId(String versionId) {
        return jdbcTemplate.query("SELECT * FROM anomaly WHERE version_id = ?", rowMapper, versionId);
    }
}
