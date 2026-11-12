package com.cashflow.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin("*")
public class SettingsController {

    private final JdbcTemplate jdbcTemplate;

    public SettingsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> getSettings() {
        Map<String, Object> settings = jdbcTemplate.queryForMap("SELECT * FROM application_settings LIMIT 1");
        String businessName = "Default Business";
        String shopkeeperPhone = "+91 98765 00000";
        String shopkeeperEmail = "shopkeeper@cashflow.com";
        try {
            businessName = jdbcTemplate.queryForObject("SELECT business_name FROM business LIMIT 1", String.class);
            if (settings.get("shopkeeper_phone") != null) {
                shopkeeperPhone = settings.get("shopkeeper_phone").toString();
            }
            if (settings.get("shopkeeper_email") != null) {
                shopkeeperEmail = settings.get("shopkeeper_email").toString();
            }
        } catch (Exception ignored) {}
        
        Map<String, Object> response = new HashMap<>(settings);
        response.put("businessName", businessName);
        response.put("shopkeeperPhone", shopkeeperPhone);
        response.put("shopkeeperEmail", shopkeeperEmail);
        response.put("phone", shopkeeperPhone);
        response.put("email", shopkeeperEmail);

        Object pe = settings.get("penalty_enabled");
        boolean penaltyEnabled = pe == null || (pe instanceof Boolean b ? b : (pe instanceof Number n ? n.intValue() != 0 : !"false".equalsIgnoreCase(pe.toString())));
        response.put("penaltyEnabled", penaltyEnabled);
        response.put("penaltyType", settings.get("penalty_type") != null ? settings.get("penalty_type").toString() : "DAILY_PERCENT");
        response.put("penaltyRate", settings.get("penalty_rate") != null ? settings.get("penalty_rate") : 0.1);
        response.put("gracePeriodDays", settings.get("grace_period_days") != null ? settings.get("grace_period_days") : 3);
        response.put("maxPenaltyPercent", settings.get("max_penalty_percent") != null ? settings.get("max_penalty_percent") : 25.0);
        return response;
    }

    @PutMapping
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> payload) {
        if (payload.containsKey("theme")) {
            jdbcTemplate.update("UPDATE application_settings SET theme = ?", payload.get("theme"));
        }
        if (payload.containsKey("forecastDays")) {
            jdbcTemplate.update("UPDATE application_settings SET forecast_days = ?", payload.get("forecastDays"));
        }
        if (payload.containsKey("minimumCashThreshold")) {
            jdbcTemplate.update("UPDATE application_settings SET minimum_cash_threshold = ?", payload.get("minimumCashThreshold"));
        }
        if (payload.containsKey("currency")) {
            jdbcTemplate.update("UPDATE application_settings SET currency = ?", payload.get("currency"));
        }
        if (payload.containsKey("penaltyEnabled")) {
            Object val = payload.get("penaltyEnabled");
            boolean enabled = val instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(val));
            jdbcTemplate.update("UPDATE application_settings SET penalty_enabled = ?", enabled ? 1 : 0);
        }
        if (payload.containsKey("penaltyType")) {
            jdbcTemplate.update("UPDATE application_settings SET penalty_type = ?", String.valueOf(payload.get("penaltyType")));
        }
        if (payload.containsKey("penaltyRate")) {
            jdbcTemplate.update("UPDATE application_settings SET penalty_rate = ?", payload.get("penaltyRate"));
        }
        if (payload.containsKey("gracePeriodDays")) {
            jdbcTemplate.update("UPDATE application_settings SET grace_period_days = ?", payload.get("gracePeriodDays"));
        }
        if (payload.containsKey("maxPenaltyPercent")) {
            jdbcTemplate.update("UPDATE application_settings SET max_penalty_percent = ?", payload.get("maxPenaltyPercent"));
        }
        if (payload.containsKey("shopkeeperPhone") || payload.containsKey("phone")) {
            Object phone = payload.containsKey("shopkeeperPhone") ? payload.get("shopkeeperPhone") : payload.get("phone");
            jdbcTemplate.update("UPDATE application_settings SET shopkeeper_phone = ?", phone);
            try { jdbcTemplate.update("UPDATE business SET phone = ?", phone); } catch (Exception ignored) {}
        }
        if (payload.containsKey("shopkeeperEmail") || payload.containsKey("email")) {
            Object email = payload.containsKey("shopkeeperEmail") ? payload.get("shopkeeperEmail") : payload.get("email");
            jdbcTemplate.update("UPDATE application_settings SET shopkeeper_email = ?", email);
            try { jdbcTemplate.update("UPDATE business SET email = ?", email); } catch (Exception ignored) {}
        }
        if (payload.containsKey("businessName")) {
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM business", Integer.class);
            if (count == 0) {
                jdbcTemplate.update("INSERT INTO business (business_id, business_name, created_at, updated_at) VALUES ('B1', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", payload.get("businessName"));
            } else {
                jdbcTemplate.update("UPDATE business SET business_name = ?, updated_at = CURRENT_TIMESTAMP", payload.get("businessName"));
            }
        }
        return getSettings();
    }

    @GetMapping("/themes")
    public List<String> getThemes() {
        return Arrays.asList(
            "theme-arctic-ledger",
            "theme-forest-ledger",
            "theme-copper-desk",
            "theme-ocean-glass",
            "theme-paper-ink"
        );
    }
}
