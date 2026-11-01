package com.cashflow.demo;

import com.cashflow.controller.CsvImportController;
import com.cashflow.controller.ScenarioController;
import com.cashflow.controller.ForecastController;
import com.cashflow.controller.DashboardController;
import com.cashflow.controller.ReceivablesController;
import com.cashflow.controller.ExpenseController;
import com.cashflow.controller.WarningController;
import com.cashflow.controller.AnomalyController;
import com.cashflow.controller.SettingsController;
import com.cashflow.controller.CommunicationController;
import com.cashflow.model.dto.CsvImportResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PermutationCombinationTest {

    @Autowired
    private CsvImportController csvController;

    @Autowired
    private ScenarioController scenarioController;

    @Autowired
    private ForecastController forecastController;

    @Autowired
    private DashboardController dashboardController;

    @Autowired
    private ReceivablesController receivablesController;

    @Autowired
    private ExpenseController expenseController;

    @Autowired
    private WarningController warningController;

    @Autowired
    private AnomalyController anomalyController;

    @Autowired
    private SettingsController settingsController;

    @Autowired
    private CommunicationController commController;

    @Test
    @DisplayName("Permutation Test: Load all 3 demo datasets in succession and verify telemetry")
    public void testDemoDatasetPermutations() {
        String[] demos = {"demo1", "demo2", "demo3"};
        for (String demo : demos) {
            ResponseEntity<?> resp = csvController.loadDemoDataset(demo);
            assertEquals(200, resp.getStatusCode().value(), "Failed loading demo: " + demo);
            CsvImportResult result = (CsvImportResult) resp.getBody();
            assertNotNull(result);
            assertTrue(result.getValidRows() > 0, "Valid rows should be > 0 for " + demo);
            String vId = result.getVersionId();
            assertNotNull(vId);

            // Verify Dashboard metrics for this specific version
            var dashResp = dashboardController.getMetrics(vId);
            assertEquals(200, dashResp.getStatusCode().value());
            assertNotNull(dashResp.getBody());

            // Verify Receivables for this specific version
            var recv = receivablesController.getReceivables(vId);
            assertNotNull(recv);

            // Verify Expenses for this specific version
            var exp = expenseController.getExpenses(vId);
            assertNotNull(exp);

            // Verify Warnings & Anomalies for this specific version
            var warn = warningController.getWarnings(vId);
            assertNotNull(warn);

            var anom = anomalyController.getAnomalies(vId);
            assertNotNull(anom);
        }
    }

    @Test
    @DisplayName("Permutation Test: Run What-If Simulation across all shock types with and without target dates")
    public void testWhatIfSimulationPermutations() {
        // Ensure baseline demo is loaded
        csvController.loadDemoDataset("demo1");

        String[] types = {"DELAY_PAYMENT", "INCREASE_EXPENSE", "DECREASE_INCOME", "ADD_EXPENSE"};

        // 1. Single Shock without target date (Day 1 fallback)
        for (String type : types) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", "Test Permutation " + type + " No Date");
            payload.put("description", "Automated stress verification");

            Map<String, Object> adj = new HashMap<>();
            adj.put("adjustmentType", type);
            adj.put("description", "Shock " + type);
            adj.put("amount", new BigDecimal("35000.00"));
            adj.put("targetDate", ""); // Blank date

            payload.put("adjustments", List.of(adj));

            Map<String, Object> created = scenarioController.createScenario(payload);
            assertNotNull(created.get("scenarioId"));
            String id = (String) created.get("scenarioId");

            Map<String, Object> forecast = scenarioController.getScenarioForecast(id);
            assertNotNull(forecast);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> base = (List<Map<String, Object>>) forecast.get("baseline");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> scen = (List<Map<String, Object>>) forecast.get("scenario");

            assertFalse(base.isEmpty(), "Baseline forecast should not be empty");
            assertFalse(scen.isEmpty(), "Scenario forecast should not be empty");
            assertEquals(base.size(), scen.size());

            // Since it is an expense or delay or income reduction, stressed closing cash should be strictly less than baseline
            BigDecimal baseFinal = (BigDecimal) base.get(base.size() - 1).get("closingCash");
            BigDecimal scenFinal = (BigDecimal) scen.get(scen.size() - 1).get("closingCash");
            assertTrue(scenFinal.compareTo(baseFinal) < 0, "Stressed cash should be less than baseline for " + type);
        }

        // 2. Compound Multi-Shock combining all 4 types simultaneously
        Map<String, Object> multiPayload = new HashMap<>();
        multiPayload.put("name", "Compound Multi-Shock Crisis");
        multiPayload.put("description", "Testing combination of delay, expense shock, income drop and inflation");

        List<Map<String, Object>> adjs = new ArrayList<>();
        adjs.add(Map.of("adjustmentType", "DELAY_PAYMENT", "description", "Client Delay", "amount", new BigDecimal("25000.00"), "targetDate", ""));
        adjs.add(Map.of("adjustmentType", "ADD_EXPENSE", "description", "Emergency Fix", "amount", new BigDecimal("40000.00"), "targetDate", ""));
        adjs.add(Map.of("adjustmentType", "DECREASE_INCOME", "description", "Demand Drop", "amount", new BigDecimal("30000.00"), "targetDate", ""));
        adjs.add(Map.of("adjustmentType", "INCREASE_EXPENSE", "description", "Utility Surge", "amount", new BigDecimal("15000.00"), "targetDate", ""));

        multiPayload.put("adjustments", adjs);
        Map<String, Object> multiCreated = scenarioController.createScenario(multiPayload);
        String multiId = (String) multiCreated.get("scenarioId");

        Map<String, Object> multiForecast = scenarioController.getScenarioForecast(multiId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mBase = (List<Map<String, Object>>) multiForecast.get("baseline");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mScen = (List<Map<String, Object>>) multiForecast.get("scenario");

        BigDecimal mBaseFinal = (BigDecimal) mBase.get(mBase.size() - 1).get("closingCash");
        BigDecimal mScenFinal = (BigDecimal) mScen.get(mScen.size() - 1).get("closingCash");
        BigDecimal expectedDelta = new BigDecimal("110000.00"); // 25k + 40k + 30k + 15k
        BigDecimal actualDelta = mBaseFinal.subtract(mScenFinal);
        assertEquals(0, expectedDelta.compareTo(actualDelta), "Total shock impact delta must match sum of all adjustments");
    }

    @Test
    @DisplayName("Permutation Test: Forward Cash Horizon (7, 30, 60, 90 Days)")
    public void testForecastHorizonPermutations() {
        csvController.loadDemoDataset("demo1");
        int[] horizons = {7, 30, 60, 90};
        for (int h : horizons) {
            ResponseEntity<?> resp = forecastController.getForecast(h, null);
            assertEquals(200, resp.getStatusCode().value());
            assertNotNull(resp.getBody());
        }
    }

    @Test
    @DisplayName("Permutation Test: Multi-Channel Communication & Bulk Dispatch")
    public void testOutreachPermutations() {
        csvController.loadDemoDataset("demo1");

        // 1. Bulk notify defaulters
        ResponseEntity<?> bulk = commController.bulkNotifyDefaulters();
        assertEquals(200, bulk.getStatusCode().value());

        // 2. Fetch logs and verify dual-party logs exist
        var logs = commController.getLogs(null);
        assertEquals(200, logs.getStatusCode().value());
        assertNotNull(logs.getBody());
        assertFalse(logs.getBody().isEmpty(), "Communication logs must have records");
    }

    @Test
    @DisplayName("Permutation Test: Settings, Profile & Themes")
    public void testSettingsPermutations() {
        String[] themes = {
            "theme-arctic-ledger", "theme-forest-ledger", "theme-copper-desk", "theme-ocean-glass", "theme-paper-ink"
        };
        for (String theme : themes) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("businessName", "Apex Corp Enterprise");
            payload.put("theme", theme);
            payload.put("currency", "INR");
            payload.put("minimumCashThreshold", 75000.0);
            payload.put("shopkeeperPhone", "+91 98765 43210");
            payload.put("shopkeeperEmail", "cfo@apexcorp.com");

            Map<String, Object> resp = settingsController.updateSettings(payload);
            assertNotNull(resp);

            Map<String, Object> curr = settingsController.getSettings();
            assertNotNull(curr);
            assertEquals(theme, curr.get("theme"));
            assertEquals("Apex Corp Enterprise", curr.get("businessName"));
        }
    }
}
