package com.cashflow.controller;

import com.cashflow.model.DatasetVersion;
import com.cashflow.model.dto.CsvImportResult;
import com.cashflow.repository.DatasetVersionRepository;
import com.cashflow.service.AnomalyService;
import com.cashflow.service.CashFlowService;
import com.cashflow.service.CsvImportService;
import com.cashflow.service.ForecastService;
import com.cashflow.service.ReceivablesService;
import com.cashflow.service.WarningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/csv")
@CrossOrigin("*")
public class CsvImportController {

    private final CsvImportService csvImportService;
    private final DatasetVersionRepository versionRepository;
    private final AnomalyService anomalyService;
    private final ReceivablesService receivablesService;
    private final ForecastService forecastService;
    private final WarningService warningService;

    public CsvImportController(CsvImportService csvImportService, DatasetVersionRepository versionRepository, AnomalyService anomalyService, ReceivablesService receivablesService, ForecastService forecastService, WarningService warningService) {
        this.csvImportService = csvImportService;
        this.versionRepository = versionRepository;
        this.anomalyService = anomalyService;
        this.receivablesService = receivablesService;
        this.forecastService = forecastService;
        this.warningService = warningService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CsvImportResult> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            CsvImportResult result = csvImportService.importCsv(file);
            if (result.getVersionId() != null) {
                anomalyService.detectAnomalies(result.getVersionId());
                receivablesService.updateCustomerMetrics(result.getVersionId());
                var forecast = forecastService.generateForecast(result.getVersionId(), 90);
                warningService.generateWarnings(result.getVersionId(), forecast, new BigDecimal("50000.0"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/demo/{demoName}")
    public ResponseEntity<?> loadDemoDataset(@PathVariable String demoName) {
        try {
            String fileName;
            switch (demoName.toLowerCase()) {
                case "demo1":
                case "baseline":
                    fileName = "dataset-1-baseline.csv";
                    break;
                case "demo2":
                case "expansion":
                    fileName = "dataset-2-expansion.csv";
                    break;
                case "demo3":
                case "stress":
                case "crisis":
                    fileName = "dataset-3-stress.csv";
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unknown demo dataset: " + demoName);
            }

            java.io.File file = new java.io.File("source/main/resources/" + fileName);
            if (!file.exists()) {
                file = new java.io.File(fileName);
            }
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            CsvImportResult result = csvImportService.importCsv(file);
            if (result.getVersionId() != null) {
                anomalyService.detectAnomalies(result.getVersionId());
                receivablesService.updateCustomerMetrics(result.getVersionId());
                var forecast = forecastService.generateForecast(result.getVersionId(), 90);
                warningService.generateWarnings(result.getVersionId(), forecast, new BigDecimal("50000.0"));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to load demo: " + e.getMessage());
        }
    }
}
