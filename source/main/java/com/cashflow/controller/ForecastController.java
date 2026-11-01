package com.cashflow.controller;

import com.cashflow.model.ForecastEntry;
import com.cashflow.repository.DatasetVersionRepository;
import com.cashflow.repository.ForecastRepository;
import com.cashflow.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
@CrossOrigin("*")
public class ForecastController {

    private final ForecastRepository forecastRepository;
    private final ForecastService forecastService;
    private final DatasetVersionRepository versionRepository;
    private final JdbcTemplate jdbcTemplate;

    public ForecastController(ForecastRepository forecastRepository, ForecastService forecastService, DatasetVersionRepository versionRepository, JdbcTemplate jdbcTemplate) {
        this.forecastRepository = forecastRepository;
        this.forecastService = forecastService;
        this.versionRepository = versionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getForecast(@RequestParam(defaultValue = "90") int days,
                                         @RequestParam(required = false) String versionId) {
        java.util.Optional<com.cashflow.model.DatasetVersion> versionOpt;
        if (versionId != null && !versionId.isBlank()) {
            versionOpt = versionRepository.findById(versionId);
            if (versionOpt.isEmpty()) {
                try {
                    int num = Integer.parseInt(versionId);
                    List<String> ids = jdbcTemplate.queryForList("SELECT version_id FROM dataset_version WHERE version_number = ? LIMIT 1", String.class, num);
                    if (!ids.isEmpty()) versionOpt = versionRepository.findById(ids.get(0));
                } catch (NumberFormatException ignored) {}
            }
        } else {
            versionOpt = versionRepository.findLatest();
        }

        return versionOpt.map(v -> {
            List<ForecastEntry> list = forecastService.generateForecast(v.getVersionId(), days);

            BigDecimal threshold = new BigDecimal("50000");
            try {
                BigDecimal t = jdbcTemplate.queryForObject("SELECT minimum_cash_threshold FROM application_settings LIMIT 1", BigDecimal.class);
                if (t != null) threshold = t;
            } catch (Exception ignored) {}

            BigDecimal minCash = list.stream().map(ForecastEntry::getClosingCash).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal maxCash = list.stream().map(ForecastEntry::getClosingCash).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal finalCash = list.isEmpty() ? BigDecimal.ZERO : list.get(list.size() - 1).getClosingCash();

            Map<String, Object> response = new HashMap<>();
            response.put("entries", list);
            response.put("threshold", threshold);
            response.put("days", days);
            response.put("versionId", v.getVersionId());
            response.put("versionNumber", v.getVersionNumber());
            response.put("minCash", minCash);
            response.put("maxCash", maxCash);
            response.put("closingCash", finalCash);

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.noContent().build());
    }
}
