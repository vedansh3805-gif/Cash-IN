package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anomaly {
    private String anomalyId;
    private String versionId;
    private String transactionId;
    private String anomalyType;
    private String severity;
    private BigDecimal expectedValue;
    private BigDecimal actualValue;
    private String explanation;
}
