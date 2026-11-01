package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Long id;
    private String transactionId;
    private String versionId;
    private LocalDate transactionDate;
    private String transactionType;
    private String category;
    private String description;
    private BigDecimal amount;
    private String customer;
    private String customerPhone;
    private String customerEmail;
    private String supplier;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private String status;
}
