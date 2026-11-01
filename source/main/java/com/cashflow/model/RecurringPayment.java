package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPayment {
    private String recurringId;
    private String description;
    private String category;
    private BigDecimal amount;
    private String frequency;
    private LocalDate nextDueDate;
    private Boolean active;
}
