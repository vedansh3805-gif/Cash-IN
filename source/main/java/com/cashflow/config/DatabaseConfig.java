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

            // Populate default shopkeeper contacts if null
            try {
                jdbcTemplate.execute("UPDATE application_settings SET shopkeeper_phone = '+91 98765 00000' WHERE shopkeeper_phone IS NULL");
                jdbcTemplate.execute("UPDATE application_settings SET shopkeeper_email = 'shopkeeper@cashflow.com' WHERE shopkeeper_email IS NULL");
            } catch (Exception ignored) {}
        };
    }
}
