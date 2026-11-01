package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSettings {
    private String settingId;
    private String theme;
    private Integer forecastDays;
    private BigDecimal minimumCashThreshold;
    private String currency;
    private String shopkeeperPhone;
    private String shopkeeperEmail;
}
