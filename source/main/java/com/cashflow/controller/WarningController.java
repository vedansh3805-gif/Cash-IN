package com.cashflow.controller;

import com.cashflow.model.Warning;
import com.cashflow.repository.DatasetVersionRepository;
import com.cashflow.repository.WarningRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/warnings")
@CrossOrigin("*")
public class WarningController {
    
    private final WarningRepository warningRepository;
    private final DatasetVersionRepository versionRepository;

    public WarningController(WarningRepository warningRepository, DatasetVersionRepository versionRepository) {
        this.warningRepository = warningRepository;
        this.versionRepository = versionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Warning>> getWarnings(@RequestParam(required = false) String versionId) {
        if (versionId != null && !versionId.isBlank()) {
            List<Warning> warnings = warningRepository.findByVersionId(versionId);
            return ResponseEntity.ok(warnings != null ? warnings : Collections.emptyList());
        }
        return versionRepository.findLatest()
                .map(v -> ResponseEntity.ok(warningRepository.findByVersionId(v.getVersionId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }
}
