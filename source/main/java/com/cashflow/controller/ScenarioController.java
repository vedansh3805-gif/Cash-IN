package com.cashflow.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/scenarios")
@CrossOrigin("*")
public class ScenarioController {

    private final JdbcTemplate jdbcTemplate;

    public ScenarioController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @PostMapping
    public Map<String, Object> createScenario(@RequestBody Map<String, Object> payload) {
        String scenarioId = UUID.randomUUID().toString();
        String name = (String) payload.get("name");
        String description = (String) payload.get("description");
        String baseVersionId = (String) payload.get("versionId");
        if (baseVersionId == null || baseVersionId.isBlank()) {
            try {
                baseVersionId = jdbcTemplate.queryForObject(
                        "SELECT version_id FROM dataset_version ORDER BY version_number DESC LIMIT 1", String.class);
            } catch (Exception ignored) {}
        }

        jdbcTemplate.update("INSERT INTO scenario (scenario_id, name, description, created_at, base_version_id) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)",
                scenarioId, name, description, baseVersionId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> adjustments = (List<Map<String, Object>>) payload.get("adjustments");
        if (adjustments != null) {
            for (Map<String, Object> adj : adjustments) {
                String adjId = UUID.randomUUID().toString();
                String type = (String) adj.get("adjustmentType");
                String desc = (String) adj.get("description");
                BigDecimal amount = toBigDecimal(adj.get("amount"));
                String targetDate = (String) adj.get("targetDate");

                jdbcTemplate.update("INSERT INTO scenario_adjustment (adjustment_id, scenario_id, adjustment_type, description, amount, target_date) VALUES (?, ?, ?, ?, ?, ?)",
                        adjId, scenarioId, type, desc, amount, targetDate);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("scenarioId", scenarioId);
        response.put("id", scenarioId);
        response.put("message", "Scenario created successfully");
        return response;
    }

    @GetMapping("/{id}/forecast")
    public Map<String, Object> getScenarioForecast(@PathVariable String id) {
        String targetVersionId = null;
        try {
            targetVersionId = jdbcTemplate.queryForObject(
                    "SELECT base_version_id FROM scenario WHERE scenario_id = ?", String.class, id);
        } catch (Exception ignored) {}

        if (targetVersionId == null || targetVersionId.isBlank()) {
            try {
                targetVersionId = jdbcTemplate.queryForObject(
                        "SELECT version_id FROM dataset_version ORDER BY version_number DESC LIMIT 1", String.class);
            } catch (Exception ignored) {}
        }

        if (targetVersionId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("baseline", Collections.emptyList());
            empty.put("scenario", Collections.emptyList());
            return empty;
        }

        List<Map<String, Object>> rawBaseline = jdbcTemplate.queryForList(
                "SELECT * FROM forecast WHERE version_id = ? ORDER BY forecast_date ASC", targetVersionId);

        List<Map<String, Object>> adjustments = jdbcTemplate.queryForList(
                "SELECT * FROM scenario_adjustment WHERE scenario_id = ?", id);

        List<Map<String, Object>> baseline = new ArrayList<>();
        List<Map<String, Object>> scenarioForecast = new ArrayList<>();
        BigDecimal cumulativeAdjustment = BigDecimal.ZERO;

        for (int i = 0; i < rawBaseline.size(); i++) {
            Map<String, Object> baseEntry = rawBaseline.get(i);
            boolean isFirst = (i == 0);
            Map<String, Object> bEntry = new HashMap<>(baseEntry);
            String fDateStr = bEntry.get("forecast_date") != null ? bEntry.get("forecast_date").toString() : "";
            BigDecimal baseClosing = toBigDecimal(bEntry.get("closing_cash"));
            BigDecimal baseInflow = toBigDecimal(bEntry.get("expected_inflow"));
            BigDecimal baseOutflow = toBigDecimal(bEntry.get("expected_outflow"));

            bEntry.put("forecastDate", fDateStr);
            bEntry.put("closingCash", baseClosing);
            bEntry.put("expectedInflow", baseInflow);
            bEntry.put("expectedOutflow", baseOutflow);
            baseline.add(bEntry);

            // Calculate Scenario Entry
            Map<String, Object> sEntry = new HashMap<>(bEntry);
            BigDecimal sInflow = baseInflow;
            BigDecimal sOutflow = baseOutflow;

            if (!fDateStr.isBlank()) {
                try {
                    LocalDate fDate = LocalDate.parse(fDateStr.length() > 10 ? fDateStr.substring(0, 10) : fDateStr);
                    for (Map<String, Object> adj : adjustments) {
                        String tDate = adj.get("target_date") != null ? adj.get("target_date").toString().trim() : "";
                        boolean matchesDate = false;
                        if (tDate.isBlank()) {
                            matchesDate = isFirst;
                        } else {
                            try {
                                LocalDate adjDate = LocalDate.parse(tDate.length() > 10 ? tDate.substring(0, 10) : tDate);
                                if (adjDate.equals(fDate) || (isFirst && adjDate.isBefore(fDate))) {
                                    matchesDate = true;
                                }
                            } catch (Exception ignored) {}
                        }

                        if (matchesDate) {
                            String type = (String) adj.get("adjustment_type");
                            BigDecimal amt = toBigDecimal(adj.get("amount"));
                            if ("ADD_EXPENSE".equalsIgnoreCase(type) || "INCREASE_EXPENSE".equalsIgnoreCase(type)) {
                                sOutflow = sOutflow.add(amt);
                                cumulativeAdjustment = cumulativeAdjustment.subtract(amt);
                            } else if ("DECREASE_INCOME".equalsIgnoreCase(type) || "DELAY_PAYMENT".equalsIgnoreCase(type)) {
                                sInflow = sInflow.subtract(amt);
                                cumulativeAdjustment = cumulativeAdjustment.subtract(amt);
                            } else {
                                cumulativeAdjustment = cumulativeAdjustment.add(amt);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            BigDecimal sClosing = baseClosing.add(cumulativeAdjustment);
            sEntry.put("forecastDate", fDateStr);
            sEntry.put("expectedInflow", sInflow);
            sEntry.put("expectedOutflow", sOutflow);
            sEntry.put("closingCash", sClosing);

            scenarioForecast.add(sEntry);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("baseline", baseline);
        result.put("scenario", scenarioForecast);
        return result;
    }
}
