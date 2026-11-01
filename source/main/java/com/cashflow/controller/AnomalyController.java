package com.cashflow.controller;

import com.cashflow.model.Anomaly;
import com.cashflow.repository.AnomalyRepository;
import com.cashflow.repository.DatasetVersionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/anomalies")
@CrossOrigin("*")
public class AnomalyController {

    private final AnomalyRepository anomalyRepository;
    private final DatasetVersionRepository versionRepository;

    public AnomalyController(AnomalyRepository anomalyRepository, DatasetVersionRepository versionRepository) {
        this.anomalyRepository = anomalyRepository;
        this.versionRepository = versionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Anomaly>> getAnomalies(@RequestParam(required = false) String versionId) {
        if (versionId != null && !versionId.isBlank()) {
            List<Anomaly> anomalies = anomalyRepository.findByVersionId(versionId);
            return ResponseEntity.ok(anomalies != null ? anomalies : Collections.emptyList());
        }
        return versionRepository.findLatest()
                .map(v -> ResponseEntity.ok(anomalyRepository.findByVersionId(v.getVersionId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }
}
