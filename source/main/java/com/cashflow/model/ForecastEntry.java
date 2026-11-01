package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastEntry {
    private String forecastId;
    private String versionId;
    private LocalDate forecastDate;
    private BigDecimal openingCash;
    private BigDecimal expectedInflow;
    private BigDecimal expectedOutflow;
    private BigDecimal closingCash;
}
