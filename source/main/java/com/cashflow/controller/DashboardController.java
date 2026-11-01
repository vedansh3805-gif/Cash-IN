package com.cashflow.controller;

import com.cashflow.model.DatasetVersion;
import com.cashflow.model.dto.DashboardMetrics;
import com.cashflow.repository.DatasetVersionRepository;
import com.cashflow.service.CashFlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
public class DashboardController {

    private final CashFlowService cashFlowService;
    private final DatasetVersionRepository versionRepository;

    public DashboardController(CashFlowService cashFlowService, DatasetVersionRepository versionRepository) {
        this.cashFlowService = cashFlowService;
        this.versionRepository = versionRepository;
    }

    @GetMapping
    public ResponseEntity<DashboardMetrics> getMetrics(@RequestParam(required = false) String versionId) {
        if (versionId != null && !versionId.isBlank()) {
            return ResponseEntity.ok(cashFlowService.calculateMetrics(versionId));
        }
        return versionRepository.findLatest()
                .map(v -> ResponseEntity.ok(cashFlowService.calculateMetrics(v.getVersionId())))
                .orElse(ResponseEntity.noContent().build());
    }
}
