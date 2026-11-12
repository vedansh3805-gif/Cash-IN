package com.cashflow.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PenaltyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("unchecked")
    void settingsReceivablesAndTransactionsExposePenaltiesEndToEnd() throws Exception {
        // 1. Settings endpoint returns penalty configuration
        MvcResult settingsRes = mockMvc.perform(get("/api/settings"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> settings = objectMapper.readValue(settingsRes.getResponse().getContentAsString(), new TypeReference<>() {});
        assertTrue(settings.containsKey("penaltyEnabled"));
        assertTrue(settings.containsKey("penaltyType"));
        assertTrue(settings.containsKey("penaltyRate"));
        assertTrue(settings.containsKey("gracePeriodDays"));
        assertTrue(settings.containsKey("maxPenaltyPercent"));

        // 2. Receivables endpoint returns accrued penalties and per-row totalPayable
        MvcResult recvRes = mockMvc.perform(get("/api/receivables"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> recvBody = objectMapper.readValue(recvRes.getResponse().getContentAsString(), new TypeReference<>() {});
        assertTrue(recvBody.containsKey("totalPenaltyAccrued"));
        assertTrue(recvBody.containsKey("totalCollectibleWithPenalty"));

        List<Map<String, Object>> receivables = (List<Map<String, Object>>) recvBody.get("receivables");
        assertNotNull(receivables);
        if (!receivables.isEmpty()) {
            Map<String, Object> first = receivables.get(0);
            assertTrue(first.containsKey("penaltyAmount"));
            assertTrue(first.containsKey("totalPayable"));
            assertTrue(first.containsKey("penaltyWaived"));

            // 3. Test waive & restore penalty on an existing receivable without leaving mutated state
            Object rowId = first.get("id");
            boolean originalWaived = Boolean.TRUE.equals(first.get("penaltyWaived"));
            MvcResult waiveRes = mockMvc.perform(post("/api/transactions/" + rowId + "/waive-penalty")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"waived\":true}"))
                    .andExpect(status().isOk())
                    .andReturn();
            Map<String, Object> waivedBody = objectMapper.readValue(waiveRes.getResponse().getContentAsString(), new TypeReference<>() {});
            assertEquals(true, waivedBody.get("penaltyWaived"));
            assertEquals(0.0, ((Number) waivedBody.get("penaltyAmount")).doubleValue(), 0.001);

            // Restore original state
            mockMvc.perform(post("/api/transactions/" + rowId + "/waive-penalty")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"waived\":" + originalWaived + "}"))
                    .andExpect(status().isOk());
        }

        // 4. Transactions endpoint returns penalty fields
        MvcResult txnRes = mockMvc.perform(get("/api/transactions?size=5"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> txnBody = objectMapper.readValue(txnRes.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Map<String, Object>> content = (List<Map<String, Object>>) txnBody.get("content");
        assertNotNull(content);
        if (!content.isEmpty()) {
            assertTrue(content.get(0).containsKey("penaltyAmount"));
            assertTrue(content.get(0).containsKey("totalPayable"));
            assertTrue(content.get(0).containsKey("daysLate"));
        }

        // 5. PUT /api/settings round-trip preserves penalty settings
        String putPayload = objectMapper.writeValueAsString(Map.of(
                "penaltyEnabled", settings.get("penaltyEnabled"),
                "penaltyType", settings.get("penaltyType"),
                "penaltyRate", settings.get("penaltyRate"),
                "gracePeriodDays", settings.get("gracePeriodDays"),
                "maxPenaltyPercent", settings.get("maxPenaltyPercent")
        ));
        mockMvc.perform(put("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putPayload))
                .andExpect(status().isOk());
    }
}
