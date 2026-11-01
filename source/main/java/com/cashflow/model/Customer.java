package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String customerId;
    private String customerName;
    private String phone;
    private String email;
    private Double averagePaymentDelay;
    private BigDecimal totalOutstanding;

    public Customer(String customerId, String customerName, Double averagePaymentDelay, BigDecimal totalOutstanding) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.averagePaymentDelay = averagePaymentDelay;
        this.totalOutstanding = totalOutstanding;
    }
}
