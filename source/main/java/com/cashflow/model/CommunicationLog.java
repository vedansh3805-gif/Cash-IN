package com.cashflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationLog {
    private String logId;
    private String transactionId;
    private String customerName;
    private String channel;        // CALL, SMS, EMAIL
    private String recipientType;  // BUYER, SHOPKEEPER, BOTH
    private String recipientContact; // Phone number or Email address
    private String subject;
    private String message;
    private String status;         // SENT, INITIATED, DELIVERED, FAILED
    private LocalDateTime timestamp;
}
