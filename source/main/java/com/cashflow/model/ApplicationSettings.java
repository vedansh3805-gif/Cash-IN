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
    private Boolean penaltyEnabled = true;
    private String penaltyType = "DAILY_PERCENT";
    private BigDecimal penaltyRate = new BigDecimal("0.1");
    private Integer gracePeriodDays = 3;
    private BigDecimal maxPenaltyPercent = new BigDecimal("25.0");

    public ApplicationSettings(String settingId, String theme, Integer forecastDays, BigDecimal minimumCashThreshold,
                               String currency, String shopkeeperPhone, String shopkeeperEmail) {
        this.settingId = settingId;
        this.theme = theme;
        this.forecastDays = forecastDays;
        this.minimumCashThreshold = minimumCashThreshold;
        this.currency = currency;
        this.shopkeeperPhone = shopkeeperPhone;
        this.shopkeeperEmail = shopkeeperEmail;
        this.penaltyEnabled = true;
        this.penaltyType = "DAILY_PERCENT";
        this.penaltyRate = new BigDecimal("0.1");
        this.gracePeriodDays = 3;
        this.maxPenaltyPercent = new BigDecimal("25.0");
    }
}
