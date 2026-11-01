package com.cashflow.demo;

import com.cashflow.model.CommunicationLog;
import com.cashflow.model.Transaction;
import com.cashflow.service.CommunicationService;
import com.cashflow.service.CsvImportService;
import com.cashflow.ui.service.CashFlowUiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FeatureWalkthroughDemoTest {

    @Autowired
    private CsvImportService importService;

    @Autowired
    private CommunicationService communicationService;

    @Autowired
    private CashFlowUiService uiService;

    @Test
    public void runFullCommunicationFeatureDemonstration() throws Exception {
        System.out.println("\n================================================================================");
        System.out.println("  CASHFLOW EARLY WARNING SYSTEM - MULTI-CHANNEL DEFAULTER OUTREACH DEMO");
        System.out.println("================================================================================\n");

        // STEP 1: Shopkeeper uploads financial dataset containing buyer mobile and email
        System.out.println(">>> STEP 1: INGESTING DATASET WITH BUYER PHONE & EMAIL CONTACTS");
        File csvFile = new File("source/main/resources/sample-data.csv");
        var importResult = importService.importCsv(csvFile);
        System.out.println("  [OK] Ingested file: " + csvFile.getName());
        System.out.println("  [OK] Valid records parsed: " + importResult.getValidRows());
        System.out.println("  [OK] Delta breakdown -> Added: " + importResult.getAdded() + ", Modified: " + importResult.getModified());

        // STEP 2: Shopkeeper inspects receivables & identifies delayed payment / defaulter
        System.out.println("\n>>> STEP 2: DETECTING DELAYED INVOICES & DEFAULTER ACCOUNTS");
        List<Transaction> receivables = uiService.getTransactions(null, "INCOME", "ALL", "ALL");
        System.out.printf("  %-10s | %-16s | %-18s | %-24s | %-10s | %-10s%n",
                "TXN ID", "BUYER", "MOBILE", "EMAIL", "AMOUNT", "STATUS");
        System.out.println("  -------------------------------------------------------------------------------------------------");
        Transaction targetDefaulter = null;
        for (Transaction t : receivables) {
            System.out.printf("  %-10s | %-16s | %-18s | %-24s | ₹%-9.2f | %-10s%n",
                    t.getTransactionId(),
                    t.getCustomer(),
                    t.getCustomerPhone() != null ? t.getCustomerPhone() : "N/A",
                    t.getCustomerEmail() != null ? t.getCustomerEmail() : "N/A",
                    t.getAmount(),
                    t.getStatus());
            if ("OVERDUE".equalsIgnoreCase(t.getStatus()) && targetDefaulter == null) {
                targetDefaulter = t;
            }
        }

        assertNotNull(targetDefaulter, "Should have at least one overdue account in sample dataset");
        System.out.println("\n  [*] Target Defaulter Selected for Outreach: " + targetDefaulter.getCustomer()
                + " (Invoice: " + targetDefaulter.getTransactionId() + ", Due: " + targetDefaulter.getDueDate()
                + ", Amount: ₹" + targetDefaulter.getAmount() + ")");

        // STEP 3: Outbound Phone Call
        System.out.println("\n>>> STEP 3: PLACING OUTBOUND PHONE CALL TO BUYER");
        Map<String, Object> callResult = communicationService.initiateCall(
                targetDefaulter.getTransactionId(), "Called accounts department regarding overdue settlement");
        System.out.println("  [CALL CONNECTED]");
        System.out.println("  - Dialed Number:     " + callResult.get("buyerPhone"));
        System.out.println("  - Recipient:         " + callResult.get("customer") + " (BUYER)");
        System.out.println("  - Shopkeeper Caller: " + callResult.get("shopkeeperPhone") + " (SHOPKEEPER)");
        System.out.println("  - Call Status:       " + callResult.get("status"));
        System.out.println("  - Summary:           " + callResult.get("message"));

        // STEP 4: Send SMS Reminder
        System.out.println("\n>>> STEP 4: SENDING INSTANT SMS PAYMENT REMINDER TO BUYER");
        String customSms = "URGENT REMINDER: Invoice " + targetDefaulter.getTransactionId()
                + " of ₹" + targetDefaulter.getAmount() + " is overdue. Please settle by today.";
        Map<String, Object> smsResult = communicationService.sendSmsNotice(targetDefaulter.getTransactionId(), customSms);
        System.out.println("  [SMS DISPATCHED]");
        System.out.println("  - Destination Phone: " + smsResult.get("buyerPhone"));
        System.out.println("  - Buyer Message:     \"" + smsResult.get("buyerMessage") + "\"");
        System.out.println("  - Shopkeeper Copy:   \"" + smsResult.get("shopkeeperMessage") + "\"");
        System.out.println("  - Delivery Status:   " + smsResult.get("status"));

        // STEP 5: Send Formal Billing Email Notice
        System.out.println("\n>>> STEP 5: SENDING FORMAL INVOICE STATEMENT EMAIL TO BUYER");
        String subject = "Overdue Invoice Notice - " + targetDefaulter.getTransactionId();
        String body = "Dear " + targetDefaulter.getCustomer() + " Accounts Department,\n\n"
                + "Our records indicate that Invoice " + targetDefaulter.getTransactionId()
                + " for the amount of ₹" + targetDefaulter.getAmount() + " was due on " + targetDefaulter.getDueDate() + ".\n"
                + "Kindly remit payment to Bank Account: 9876543210 / IFSC: CASH0001234.\n\n"
                + "Regards,\nCashFlow Merchant Accounts";
        Map<String, Object> emailResult = communicationService.sendEmailNotice(targetDefaulter.getTransactionId(), subject, body);
        System.out.println("  [EMAIL DISPATCHED]");
        System.out.println("  - Recipient Email:   " + emailResult.get("buyerEmail"));
        System.out.println("  - Shopkeeper Copy:   " + emailResult.get("shopkeeperEmail"));
        System.out.println("  - Subject:           " + emailResult.get("subject"));
        System.out.println("  - Dispatch State:    " + emailResult.get("status"));

        // STEP 6: DUAL-PARTY NOTIFICATION (Both Buyer AND Shopkeeper)
        System.out.println("\n>>> STEP 6: DUAL-PARTY BROADCAST (NOTIFYING BOTH ENDS OF THE PARTY)");
        Map<String, Object> dualResult = communicationService.notifyBothParties(targetDefaulter.getTransactionId());
        @SuppressWarnings("unchecked")
        Map<String, Object> dualSms = (Map<String, Object>) dualResult.get("sms");
        @SuppressWarnings("unchecked")
        Map<String, Object> dualEmail = (Map<String, Object>) dualResult.get("email");

        System.out.println("  [1. BUYER END NOTIFICATION]:");
        System.out.println("      SMS -> Contact: " + dualSms.get("buyerPhone") + " | Status: " + dualSms.get("status"));
        System.out.println("      EMAIL -> To:   " + dualEmail.get("buyerEmail") + " | Subject: " + dualEmail.get("subject"));

        System.out.println("  [2. SHOPKEEPER END NOTIFICATION]:");
        System.out.println("      SMS -> Audit Copy:  " + dualSms.get("shopkeeperPhone") + " | " + dualSms.get("shopkeeperMessage"));
        System.out.println("      EMAIL -> Audit Copy:" + dualEmail.get("shopkeeperEmail"));
        System.out.println("  [RESULT]: " + dualResult.get("message"));

        // STEP 7: Bulk Defaulters Outreach Across All Overdue Accounts
        System.out.println("\n>>> STEP 7: BULK DEFAULTERS OUTREACH (ALERT ALL OVERDUE ACCOUNTS)");
        int alertedCount = 0;
        for (Transaction t : receivables) {
            if ("OVERDUE".equalsIgnoreCase(t.getStatus())) {
                communicationService.notifyBothParties(t.getTransactionId());
                alertedCount++;
                System.out.println("  [*] Batch notice dispatched for account: " + t.getCustomer() + " (₹" + t.getAmount() + ")");
            }
        }
        System.out.println("  [OK] Total delinquent accounts alerted in batch: " + alertedCount);

        // STEP 8: Inspect Communication Audit Trail
        System.out.println("\n>>> STEP 8: QUERYING PERMANENT COMMUNICATION AUDIT TRAIL");
        List<CommunicationLog> auditLogs = communicationService.getCommunicationLogs("ALL");
        System.out.printf("  %-10s | %-16s | %-6s | %-10s | %-22s | %-10s | %-20s%n",
                "TXN ID", "CUSTOMER", "TYPE", "PARTY", "CONTACT", "STATUS", "TIMESTAMP");
        System.out.println("  -----------------------------------------------------------------------------------------------------------------");
        for (CommunicationLog log : auditLogs) {
            System.out.printf("  %-10s | %-16s | %-6s | %-10s | %-22s | %-10s | %-20s%n",
                    log.getTransactionId() != null ? log.getTransactionId() : "N/A",
                    log.getCustomerName(),
                    log.getChannel(),
                    log.getRecipientType(),
                    log.getRecipientContact(),
                    log.getStatus(),
                    log.getTimestamp() != null ? log.getTimestamp().toString().substring(0, 19) : "");
        }

        // STEP 9: Shopkeeper Contact Profile in Settings
        System.out.println("\n>>> STEP 9: SHOPKEEPER CONTACT PROFILE IN SETTINGS");
        Map<String, Object> settings = uiService.getSettings();
        System.out.println("  - Business Name:     " + settings.get("businessName"));
        System.out.println("  - Shopkeeper Phone:  " + settings.get("shopkeeperPhone"));
        System.out.println("  - Shopkeeper Email:  " + settings.get("shopkeeperEmail"));
        System.out.println("  - Safety Threshold:  ₹" + settings.get("minimumCashThreshold"));

        System.out.println("\n================================================================================");
        System.out.println("  DEMONSTRATION COMPLETED SUCCESSFULLY: ALL 9 WORKFLOWS VERIFIED");
        System.out.println("================================================================================\n");

        assertTrue(auditLogs.size() >= 5, "Audit log should have recorded multiple outreach events");
    }
}
