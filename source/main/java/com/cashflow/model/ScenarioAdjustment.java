package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioAdjustment {
    private String adjustmentId;
    private String scenarioId;
    private String adjustmentType;
    private String description;
    private BigDecimal amount;
    private LocalDate targetDate;
}
