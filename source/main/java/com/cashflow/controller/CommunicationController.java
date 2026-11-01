package com.cashflow.controller;

import com.cashflow.model.CommunicationLog;
import com.cashflow.service.CommunicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/communication")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @PostMapping("/call")
    public ResponseEntity<Map<String, Object>> initiateCall(@RequestBody Map<String, String> payload) {
        String transactionId = payload.get("transactionId");
        String notes = payload.get("notes");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "transactionId is required"));
        }
        Map<String, Object> result = communicationService.initiateCall(transactionId, notes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sms")
    public ResponseEntity<Map<String, Object>> sendSms(@RequestBody Map<String, String> payload) {
        String transactionId = payload.get("transactionId");
        String message = payload.get("message");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "transactionId is required"));
        }
        Map<String, Object> result = communicationService.sendSmsNotice(transactionId, message);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, String> payload) {
        String transactionId = payload.get("transactionId");
        String subject = payload.get("subject");
        String body = payload.get("body");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "transactionId is required"));
        }
        Map<String, Object> result = communicationService.sendEmailNotice(transactionId, subject, body);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/notify-both")
    public ResponseEntity<Map<String, Object>> notifyBothParties(@RequestBody Map<String, String> payload) {
        String transactionId = payload.get("transactionId");
        if (transactionId == null || transactionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "transactionId is required"));
        }
        Map<String, Object> result = communicationService.notifyBothParties(transactionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bulk-notify")
    public ResponseEntity<Map<String, Object>> bulkNotifyDefaulters() {
        Map<String, Object> result = communicationService.bulkNotifyDefaulters();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<CommunicationLog>> getLogs(@RequestParam(required = false) String transactionId) {
        return ResponseEntity.ok(communicationService.getCommunicationLogs(transactionId));
    }
}
