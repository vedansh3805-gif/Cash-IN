package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
    private String scenarioId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String baseVersionId;
}
