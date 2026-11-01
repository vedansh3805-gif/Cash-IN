package com.cashflow.repository;

import com.cashflow.model.ForecastEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ForecastRepository {
    private final JdbcTemplate jdbcTemplate;

    public ForecastRepository(JdbcTemplate jdbcTemplate) {
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

    private RowMapper<ForecastEntry> rowMapper = (rs, rowNum) -> {
        ForecastEntry f = new ForecastEntry();
        f.setForecastId(rs.getString("forecast_id"));
        f.setVersionId(rs.getString("version_id"));
        f.setForecastDate(parseDate(rs.getString("forecast_date")));
        f.setOpeningCash(rs.getBigDecimal("opening_cash"));
        f.setExpectedInflow(rs.getBigDecimal("expected_inflow"));
        f.setExpectedOutflow(rs.getBigDecimal("expected_outflow"));
        f.setClosingCash(rs.getBigDecimal("closing_cash"));
        return f;
    };

    public void saveAll(List<ForecastEntry> entries) {
        if (entries == null || entries.isEmpty()) return;
        String versionId = entries.get(0).getVersionId();
        try {
            jdbcTemplate.update("DELETE FROM forecast WHERE version_id = ?", versionId);
        } catch (Exception ignored) {}

        String sql = "INSERT INTO forecast (forecast_id, version_id, forecast_date, opening_cash, expected_inflow, expected_outflow, closing_cash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, entries, 100, (ps, f) -> {
            ps.setString(1, f.getForecastId());
            ps.setString(2, f.getVersionId());
            ps.setObject(3, f.getForecastDate());
            ps.setBigDecimal(4, f.getOpeningCash());
            ps.setBigDecimal(5, f.getExpectedInflow());
            ps.setBigDecimal(6, f.getExpectedOutflow());
            ps.setBigDecimal(7, f.getClosingCash());
        });
    }

    public List<ForecastEntry> findByVersionId(String versionId) {
        return jdbcTemplate.query("SELECT * FROM forecast WHERE version_id = ? ORDER BY forecast_date", rowMapper, versionId);
    }
}
