package com.cashflow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DatabaseConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
    }

    @Bean
    public CommandLineRunner databaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            // Safe ALTER TABLE executions to migrate existing SQLite databases
            String[] alterStatements = new String[]{
                    "ALTER TABLE transaction_record ADD COLUMN customer_phone TEXT",
                    "ALTER TABLE transaction_record ADD COLUMN customer_email TEXT",
                    "ALTER TABLE customer ADD COLUMN phone TEXT",
                    "ALTER TABLE customer ADD COLUMN email TEXT",
                    "ALTER TABLE application_settings ADD COLUMN shopkeeper_phone TEXT",
                    "ALTER TABLE application_settings ADD COLUMN shopkeeper_email TEXT",
                    "ALTER TABLE application_settings ADD COLUMN penalty_enabled BOOLEAN DEFAULT 1",
                    "ALTER TABLE application_settings ADD COLUMN penalty_type TEXT DEFAULT 'DAILY_PERCENT'",
                    "ALTER TABLE application_settings ADD COLUMN penalty_rate REAL DEFAULT 0.1",
                    "ALTER TABLE application_settings ADD COLUMN grace_period_days INTEGER DEFAULT 3",
                    "ALTER TABLE application_settings ADD COLUMN max_penalty_percent REAL DEFAULT 25.0",
                    "ALTER TABLE transaction_record ADD COLUMN penalty_amount REAL DEFAULT 0.0",
                    "ALTER TABLE transaction_record ADD COLUMN penalty_waived BOOLEAN DEFAULT 0",
                    "ALTER TABLE customer ADD COLUMN total_penalty REAL DEFAULT 0.0",
                    "ALTER TABLE business ADD COLUMN phone TEXT",
                    "ALTER TABLE business ADD COLUMN email TEXT"
            };

            for (String sql : alterStatements) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception ignored) {
                    // Column already exists in SQLite table
                }
            }

            // Ensure communication_log table exists
            try {
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS communication_log (" +
                        "log_id TEXT PRIMARY KEY, " +
                        "transaction_id TEXT, " +
                        "customer_name TEXT, " +
                        "channel TEXT, " +
                        "recipient_type TEXT, " +
                        "recipient_contact TEXT, " +
                        "subject TEXT, " +
                        "message TEXT, " +
                        "status TEXT, " +
                        "timestamp TIMESTAMP)");
            } catch (Exception ignored) {}

            // Populate default shopkeeper contacts and penalty rules if null
            try {
                jdbcTemplate.execute("UPDATE application_settings SET shopkeeper_phone = '+91 98765 00000' WHERE shopkeeper_phone IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET shopkeeper_email = 'shopkeeper@cashflow.com' WHERE shopkeeper_email IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET penalty_enabled = 1 WHERE penalty_enabled IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET penalty_type = 'DAILY_PERCENT' WHERE penalty_type IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET penalty_rate = 0.1 WHERE penalty_rate IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET grace_period_days = 3 WHERE grace_period_days IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET max_penalty_percent = 25.0 WHERE max_penalty_percent IS NULL");
                jdbcTemplate.execute("UPDATE transaction_record SET penalty_amount = 0.0 WHERE penalty_amount IS NULL");
                jdbcTemplate.execute("UPDATE transaction_record SET penalty_waived = 0 WHERE penalty_waived IS NULL");
                jdbcTemplate.execute("UPDATE customer SET total_penalty = 0.0 WHERE total_penalty IS NULL");
            } catch (Exception ignored) {}
        };
    }
}
