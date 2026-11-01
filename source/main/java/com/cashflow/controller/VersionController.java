package com.cashflow.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class VersionController {

    private final JdbcTemplate jdbcTemplate;

    public VersionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String resolveVersionId(String id) {
        if (id == null) return null;
        try {
            int num = Integer.parseInt(id);
            List<String> ids = jdbcTemplate.queryForList("SELECT version_id FROM dataset_version WHERE version_number = ? LIMIT 1", String.class, num);
            if (!ids.isEmpty()) return ids.get(0);
        } catch (NumberFormatException ignored) {}
        return id;
    }

    @GetMapping("/versions")
    public List<Map<String, Object>> getVersions() {
        return jdbcTemplate.queryForList("SELECT version_id, version_number, file_name, upload_date, record_count, previous_version_id FROM dataset_version ORDER BY version_number DESC");
    }

    @GetMapping("/versions/{id}")
    public Map<String, Object> getVersionDetail(@PathVariable String id) {
        String resolvedId = resolveVersionId(id);
        List<Map<String, Object>> versions = jdbcTemplate.queryForList("SELECT * FROM dataset_version WHERE version_id = ?", resolvedId);
        if (versions.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> version = versions.get(0);

        BigDecimal totalIncome = jdbcTemplate.queryForObject("SELECT SUM(amount) FROM transaction_record WHERE version_id = ? AND transaction_type = 'INCOME'", BigDecimal.class, resolvedId);
        BigDecimal totalExpenses = jdbcTemplate.queryForObject("SELECT SUM(amount) FROM transaction_record WHERE version_id = ? AND transaction_type = 'EXPENSE'", BigDecimal.class, resolvedId);

        List<Map<String, Object>> changes = jdbcTemplate.queryForList("SELECT change_type, COUNT(*) as cnt FROM data_change WHERE version_id = ? GROUP BY change_type", resolvedId);
        Map<String, Integer> changeSummary = new HashMap<>();
        changeSummary.put("added", 0);
        changeSummary.put("modified", 0);
        changeSummary.put("removed", 0);
        changeSummary.put("unchanged", 0);

        for (Map<String, Object> row : changes) {
            String type = ((String) row.get("change_type")).toLowerCase();
            changeSummary.put(type, ((Number) row.get("cnt")).intValue());
        }

        Map<String, Object> response = new HashMap<>(version);
        response.put("versionNumber", version.get("version_number"));
        response.put("fileName", version.get("file_name"));
        response.put("uploadDate", version.get("upload_date"));
        response.put("recordCount", version.get("record_count"));
        response.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);
        response.put("totalExpenses", totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
        response.put("changeSummary", changeSummary);

        return response;
    }

    @GetMapping("/versions/{id}/changes")
    public List<Map<String, Object>> getVersionChanges(@PathVariable String id) {
        String resolvedId = resolveVersionId(id);
        return jdbcTemplate.queryForList("SELECT * FROM data_change WHERE version_id = ? ORDER BY changed_at DESC", resolvedId);
    }

    @GetMapping("/versions/compare")
    public Map<String, Object> compareVersions(@RequestParam String v1, @RequestParam String v2) {
        String resolvedV1 = resolveVersionId(v1);
        String resolvedV2 = resolveVersionId(v2);

        List<Map<String, Object>> tx1 = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ?", resolvedV1);
        List<Map<String, Object>> tx2 = jdbcTemplate.queryForList("SELECT * FROM transaction_record WHERE version_id = ?", resolvedV2);

        Map<String, Map<String, Object>> map1 = new HashMap<>();
        for (Map<String, Object> t : tx1) {
            map1.put(String.valueOf(t.get("transaction_id")), t);
        }
        Map<String, Map<String, Object>> map2 = new HashMap<>();
        for (Map<String, Object> t : tx2) {
            map2.put(String.valueOf(t.get("transaction_id")), t);
        }

        List<Map<String, Object>> changes = new java.util.ArrayList<>();
        int added = 0;
        int modified = 0;
        int removed = 0;
        int unchanged = 0;

        for (Map.Entry<String, Map<String, Object>> entry : map2.entrySet()) {
            String tid = entry.getKey();
            Map<String, Object> t2 = entry.getValue();
            if (!map1.containsKey(tid)) {
                added++;
                Map<String, Object> change = new HashMap<>();
                change.put("transactionId", tid);
                change.put("changeType", "ADDED");
                change.put("fieldName", "Record");
                change.put("oldValue", "—");
                change.put("newValue", "₹" + t2.get("amount") + " (" + t2.get("category") + ")");
                change.put("changedAt", t2.get("transaction_date"));
                changes.add(change);
            } else {
                Map<String, Object> t1 = map1.get(tid);
                boolean isMod = false;
                if (t1.get("amount") != null && t2.get("amount") != null &&
                        new BigDecimal(t1.get("amount").toString()).compareTo(new BigDecimal(t2.get("amount").toString())) != 0) {
                    isMod = true;
                    Map<String, Object> change = new HashMap<>();
                    change.put("transactionId", tid);
                    change.put("changeType", "MODIFIED");
                    change.put("fieldName", "amount");
                    change.put("oldValue", "₹" + t1.get("amount"));
                    change.put("newValue", "₹" + t2.get("amount"));
                    change.put("changedAt", t2.get("transaction_date"));
                    changes.add(change);
                }
                if (!String.valueOf(t1.get("status")).equalsIgnoreCase(String.valueOf(t2.get("status")))) {
                    isMod = true;
                    Map<String, Object> change = new HashMap<>();
                    change.put("transactionId", tid);
                    change.put("changeType", "MODIFIED");
                    change.put("fieldName", "status");
                    change.put("oldValue", t1.get("status"));
                    change.put("newValue", t2.get("status"));
                    change.put("changedAt", t2.get("transaction_date"));
                    changes.add(change);
                }
                if (isMod) {
                    modified++;
                } else {
                    unchanged++;
                }
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : map1.entrySet()) {
            String tid = entry.getKey();
            if (!map2.containsKey(tid)) {
                removed++;
                Map<String, Object> change = new HashMap<>();
                change.put("transactionId", tid);
                change.put("changeType", "REMOVED");
                change.put("fieldName", "Record");
                change.put("oldValue", "₹" + entry.getValue().get("amount"));
                change.put("newValue", "—");
                change.put("changedAt", entry.getValue().get("transaction_date"));
                changes.add(change);
            }
        }

        Map<String, Integer> changeSummary = new HashMap<>();
        changeSummary.put("added", added);
        changeSummary.put("modified", modified);
        changeSummary.put("removed", removed);
        changeSummary.put("unchanged", unchanged);

        Map<String, Object> result = new HashMap<>();
        result.put("version1", v1);
        result.put("version2", v2);
        result.put("added", added);
        result.put("modified", modified);
        result.put("removed", removed);
        result.put("unchanged", unchanged);
        result.put("changeSummary", changeSummary);
        result.put("summary", changeSummary);
        result.put("changes", changes);
        return result;
    }

    @GetMapping("/timeline")
    public List<Map<String, Object>> getTimeline() {
        return jdbcTemplate.queryForList("SELECT version_id, version_number, file_name, upload_date, record_count FROM dataset_version ORDER BY version_number ASC");
    }
}
