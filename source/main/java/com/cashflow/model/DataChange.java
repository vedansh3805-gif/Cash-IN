package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataChange {
    private String changeId;
    private String versionId;
    private String transactionId;
    private String changeType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}
