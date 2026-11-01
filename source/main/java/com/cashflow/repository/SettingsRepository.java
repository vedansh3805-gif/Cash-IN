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
        return s;
    };

    public ApplicationSettings getSettings() {
        List<ApplicationSettings> list = jdbcTemplate.query("SELECT * FROM application_settings WHERE setting_id = 'DEFAULT'", rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public void update(ApplicationSettings settings) {
        String sql = "UPDATE application_settings SET theme = ?, forecast_days = ?, minimum_cash_threshold = ?, currency = ?, shopkeeper_phone = ?, shopkeeper_email = ? WHERE setting_id = 'DEFAULT'";
        jdbcTemplate.update(sql, settings.getTheme(), settings.getForecastDays(), settings.getMinimumCashThreshold(), settings.getCurrency(), settings.getShopkeeperPhone(), settings.getShopkeeperEmail());
    }
}
