package com.cashflow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportResult {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private int added;
    private int modified;
    private int removed;
    private int unchanged;
    private int versionNumber;
    private List<String> errors;
    private String versionId;

    public CsvImportResult(int validRows, int invalidRows, List<String> errors, String versionId) {
        this.totalRows = validRows + invalidRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
        this.errors = errors;
        this.versionId = versionId;
    }
}
