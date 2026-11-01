package com.cashflow.repository;

import com.cashflow.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Customer> rowMapper = (rs, rowNum) -> {
        Customer c = new Customer();
        c.setCustomerId(rs.getString("customer_id"));
        c.setCustomerName(rs.getString("customer_name"));
        try { c.setPhone(rs.getString("phone")); } catch (Exception ignored) {}
        try { c.setEmail(rs.getString("email")); } catch (Exception ignored) {}
        c.setAveragePaymentDelay(rs.getDouble("average_payment_delay"));
        c.setTotalOutstanding(rs.getBigDecimal("total_outstanding"));
        return c;
    };

    public List<Customer> findAll() {
        return jdbcTemplate.query("SELECT * FROM customer", rowMapper);
    }

    public void save(Customer c) {
        String sql = "INSERT INTO customer (customer_id, customer_name, phone, email, average_payment_delay, total_outstanding) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, c.getCustomerId(), c.getCustomerName(), c.getPhone(), c.getEmail(), c.getAveragePaymentDelay(), c.getTotalOutstanding());
    }
    
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM customer");
    }
}
