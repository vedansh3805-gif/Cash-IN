package com.cashflow.repository;

import com.cashflow.model.ApplicationSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SettingsRepository {
    private final JdbcTemplate jdbcTemplate;

    public SettingsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<ApplicationSettings> rowMapper = (rs, rowNum) -> {
        ApplicationSettings s = new ApplicationSettings();
        s.setSettingId(rs.getString("setting_id"));
        s.setTheme(rs.getString("theme"));
        s.setForecastDays(rs.getInt("forecast_days"));
        s.setMinimumCashThreshold(rs.getBigDecimal("minimum_cash_threshold"));
        s.setCurrency(rs.getString("currency"));
        try { s.setShopkeeperPhone(rs.getString("shopkeeper_phone")); } catch (Exception ignored) {}
        try { s.setShopkeeperEmail(rs.getString("shopkeeper_email")); } catch (Exception ignored) {}
        if (s.getShopkeeperPhone() == null || s.getShopkeeperPhone().isBlank()) {
            s.setShopkeeperPhone("+91 98765 00000");
        }
        if (s.getShopkeeperEmail() == null || s.getShopkeeperEmail().isBlank()) {
            s.setShopkeeperEmail("shopkeeper@cashflow.com");
        }
        try { s.setPenaltyEnabled(rs.getObject("penalty_enabled") == null ? Boolean.TRUE : rs.getBoolean("penalty_enabled")); } catch (Exception ignored) {}
        try { s.setPenaltyType(rs.getString("penalty_type")); } catch (Exception ignored) {}
        try { s.setPenaltyRate(rs.getBigDecimal("penalty_rate")); } catch (Exception ignored) {}
        try { s.setGracePeriodDays(rs.getObject("grace_period_days") == null ? 3 : rs.getInt("grace_period_days")); } catch (Exception ignored) {}
        try { s.setMaxPenaltyPercent(rs.getBigDecimal("max_penalty_percent")); } catch (Exception ignored) {}
        if (s.getPenaltyEnabled() == null) s.setPenaltyEnabled(true);
        if (s.getPenaltyType() == null || s.getPenaltyType().isBlank()) s.setPenaltyType("DAILY_PERCENT");
        if (s.getPenaltyRate() == null) s.setPenaltyRate(new java.math.BigDecimal("0.1"));
        if (s.getGracePeriodDays() == null) s.setGracePeriodDays(3);
        if (s.getMaxPenaltyPercent() == null) s.setMaxPenaltyPercent(new java.math.BigDecimal("25.0"));
        return s;
    };

    public ApplicationSettings getSettings() {
        List<ApplicationSettings> list = jdbcTemplate.query("SELECT * FROM application_settings WHERE setting_id = 'DEFAULT'", rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public void update(ApplicationSettings settings) {
        String sql = "UPDATE application_settings SET theme = ?, forecast_days = ?, minimum_cash_threshold = ?, currency = ?, shopkeeper_phone = ?, shopkeeper_email = ?, penalty_enabled = ?, penalty_type = ?, penalty_rate = ?, grace_period_days = ?, max_penalty_percent = ? WHERE setting_id = 'DEFAULT'";
        jdbcTemplate.update(sql, settings.getTheme(), settings.getForecastDays(), settings.getMinimumCashThreshold(), settings.getCurrency(), settings.getShopkeeperPhone(), settings.getShopkeeperEmail(),
                settings.getPenaltyEnabled() != null ? settings.getPenaltyEnabled() : true,
                settings.getPenaltyType() != null ? settings.getPenaltyType() : "DAILY_PERCENT",
                settings.getPenaltyRate() != null ? settings.getPenaltyRate() : new java.math.BigDecimal("0.1"),
                settings.getGracePeriodDays() != null ? settings.getGracePeriodDays() : 3,
                settings.getMaxPenaltyPercent() != null ? settings.getMaxPenaltyPercent() : new java.math.BigDecimal("25.0"));
    }
}
