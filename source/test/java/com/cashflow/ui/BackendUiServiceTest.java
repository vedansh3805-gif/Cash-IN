package com.cashflow.ui;

import com.cashflow.model.dto.DashboardMetrics;
import com.cashflow.ui.service.CashFlowUiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BackendUiServiceTest {

    @Autowired
    private CashFlowUiService uiService;

    @Test
    public void testGetDashboardMetrics() {
        DashboardMetrics m = uiService.getDashboardMetrics();
        assertNotNull(m);
        assertNotNull(m.getCurrentCash());
        assertNotNull(m.getRiskStatus());
    }

    @Test
    public void testGetTransactions() {
        var txns = uiService.getTransactions(null, "ALL", "ALL", "ALL");
        assertNotNull(txns);
    }

    @Test
    public void testGetForecast() {
        var forecast = uiService.getForecast(30);
        assertNotNull(forecast);
    }

    @Test
    public void testGetWarningsAndAnomalies() {
        var warnings = uiService.getWarnings();
        assertNotNull(warnings);

        var anomalies = uiService.getAnomalies();
        assertNotNull(anomalies);
    }

    @Test
    public void testReceivablesAndExpenseAnalysis() {
        Map<String, Object> rec = uiService.getReceivablesAnalysis();
        assertNotNull(rec);
        assertTrue(rec.containsKey("totalOutstanding"));

        Map<String, Object> exp = uiService.getExpenseAnalysis();
        assertNotNull(exp);
        assertTrue(exp.containsKey("totalExpense"));
    }

    @Test
    public void testScenarioSimulation() {
        Map<String, Object> sim = uiService.runScenarioSimulation(
                "Test Shock", "DELAY_RECEIVABLES", new BigDecimal("50000"), 15);
        assertNotNull(sim);
        assertTrue(sim.containsKey("baseline"));
        assertTrue(sim.containsKey("scenario"));
    }

    @Test
    public void testSettings() {
        Map<String, Object> s = uiService.getSettings();
        assertNotNull(s);
        assertTrue(s.containsKey("theme"));
        assertTrue(s.containsKey("minimumCashThreshold"));
        assertTrue(s.containsKey("shopkeeperPhone"));
        assertTrue(s.containsKey("shopkeeperEmail"));
    }

    @Test
    public void testRecurringPaymentsLifecycle() {
        int initialCount = uiService.getRecurringPayments().size();
        uiService.addRecurringPayment("Test Cloud Server", "Software", new BigDecimal("4500.00"), "MONTHLY", "2026-11-01");
        List<Map<String, Object>> list = uiService.getRecurringPayments();
        assertEquals(initialCount + 1, list.size());

        Map<String, Object> created = list.stream()
                .filter(r -> "Test Cloud Server".equals(r.get("description")))
                .findFirst().orElseThrow();
        assertEquals("Software", created.get("category"));

        uiService.deleteRecurringPayment(String.valueOf(created.get("recurringId")));
        assertEquals(initialCount, uiService.getRecurringPayments().size());
    }

    @Test
    public void testScenarioSimulationTypes() {
        String[] types = {"DELAY_RECEIVABLES", "INCREASE_EXPENSES", "UNEXPECTED_EXPENSE", "REDUCE_EXPENSES", "INCREASE_REVENUE"};
        for (String type : types) {
            Map<String, Object> sim = uiService.runScenarioSimulation(
                    "Test " + type, type, new BigDecimal("25000"), 10);
            assertNotNull(sim, "Failed for type: " + type);
            assertTrue(sim.containsKey("baseline"), "Missing baseline for type: " + type);
            assertTrue(sim.containsKey("scenario"), "Missing scenario for type: " + type);
        }
    }

    @Test
    public void testCommunicationLogs() {
        var logs = uiService.getCommunicationLogs("ALL");
        assertNotNull(logs);
    }

    @Test
    public void testBulkNotifyDefaulters() {
        var res = uiService.bulkNotifyDefaulters();
        assertNotNull(res);
        assertTrue(res.containsKey("success"));
        assertTrue(res.containsKey("notifiedCount"));
    }
}
