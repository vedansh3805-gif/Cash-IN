package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasetVersion {
    private String versionId;
    private Integer versionNumber;
    private String fileName;
    private LocalDateTime uploadDate;
    private Integer recordCount;
    private String previousVersionId;
}
