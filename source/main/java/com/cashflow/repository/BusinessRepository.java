package com.cashflow.repository;

import com.cashflow.model.Business;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessRepository {
    private final JdbcTemplate jdbcTemplate;

    public BusinessRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
