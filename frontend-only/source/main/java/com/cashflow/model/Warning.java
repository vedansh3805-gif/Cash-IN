package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warning {
    private String warningId;
    private String versionId;
    private String warningType;
    private String severity;
    private LocalDate warningDate;
    private LocalDate projectedDate;
    private String message;
    private String status;
}
