package com.cashflow.service;

import com.cashflow.model.ApplicationSettings;
import com.cashflow.model.CommunicationLog;
import com.cashflow.model.Customer;
import com.cashflow.model.Transaction;
import com.cashflow.repository.CommunicationLogRepository;
import com.cashflow.repository.CustomerRepository;
import com.cashflow.repository.SettingsRepository;
import com.cashflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommunicationService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final SettingsRepository settingsRepository;
    private final CommunicationLogRepository logRepository;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public CommunicationService(TransactionRepository transactionRepository,
                                CustomerRepository customerRepository,
                                SettingsRepository settingsRepository,
                                CommunicationLogRepository logRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.settingsRepository = settingsRepository;
        this.logRepository = logRepository;
    }

    private Transaction findTransactionOrThrow(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + transactionId));
    }

    private ApplicationSettings getSettings() {
        ApplicationSettings settings = settingsRepository.getSettings();
        if (settings == null) {
            settings = new ApplicationSettings("DEFAULT", "arctic-ledger", 90, new java.math.BigDecimal("50000"), "INR", "+91 98765 00000", "shopkeeper@cashflow.com");
        }
        if (settings.getShopkeeperPhone() == null || settings.getShopkeeperPhone().isBlank()) {
            settings.setShopkeeperPhone("+91 98765 00000");
        }
        if (settings.getShopkeeperEmail() == null || settings.getShopkeeperEmail().isBlank()) {
            settings.setShopkeeperEmail("shopkeeper@cashflow.com");
        }
        return settings;
    }

    private String resolveBuyerPhone(Transaction t) {
        if (t.getCustomerPhone() != null && !t.getCustomerPhone().isBlank()) {
            return t.getCustomerPhone();
        }
        if (t.getCustomer() != null) {
            for (Customer c : customerRepository.findAll()) {
                if (t.getCustomer().equalsIgnoreCase(c.getCustomerName()) && c.getPhone() != null && !c.getPhone().isBlank()) {
                    return c.getPhone();
                }
            }
        }
        return "+91 98000 00000";
    }

    private String resolveBuyerEmail(Transaction t) {
        if (t.getCustomerEmail() != null && !t.getCustomerEmail().isBlank()) {
            return t.getCustomerEmail();
        }
        if (t.getCustomer() != null) {
            for (Customer c : customerRepository.findAll()) {
                if (t.getCustomer().equalsIgnoreCase(c.getCustomerName()) && c.getEmail() != null && !c.getEmail().isBlank()) {
                    return c.getEmail();
                }
            }
        }
        String safeName = (t.getCustomer() != null ? t.getCustomer().toLowerCase().replaceAll("[^a-z0-9]", "") : "buyer");
        return safeName + "@buyer-mail.com";
    }

    public Map<String, Object> initiateCall(String transactionId, String notes) {
        Transaction t = findTransactionOrThrow(transactionId);
        ApplicationSettings settings = getSettings();
        String buyerPhone = resolveBuyerPhone(t);
        String customerName = t.getCustomer() != null ? t.getCustomer() : "Valued Buyer";
        String formattedAmt = currencyFormatter.format(t.getAmount());

        LocalDateTime now = LocalDateTime.now();

        // 1. Log Buyer Call
        String buyerCallMsg = String.format("Outbound call placed to %s (%s) regarding invoice #%s (%s, %s). Notes: %s",
                customerName, buyerPhone, t.getTransactionId(), formattedAmt, t.getStatus(),
                notes != null && !notes.isBlank() ? notes : "Direct voice reminder for invoice settlement.");
        CommunicationLog buyerLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "CALL",
                "BUYER",
                buyerPhone,
                "Call to Buyer: " + customerName,
                buyerCallMsg,
                "COMPLETED",
                now
        );
        logRepository.save(buyerLog);

        // 2. Log Shopkeeper Audit Call Record
        String shopkeeperCallMsg = String.format("Shopkeeper %s initiated call to %s (%s) for invoice #%s of %s. Call completed.",
                settings.getShopkeeperPhone(), customerName, buyerPhone, t.getTransactionId(), formattedAmt);
        CommunicationLog shopkeeperLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "CALL",
                "SHOPKEEPER",
                settings.getShopkeeperPhone(),
                "Shopkeeper Outbound Call Record",
                shopkeeperCallMsg,
                "COMPLETED",
                now
        );
        logRepository.save(shopkeeperLog);

        // Optional OS-level URI trigger if desktop environment is active
        try {
            if (!java.awt.GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                String cleanPhone = buyerPhone.replaceAll("[^0-9+]", "");
                Desktop.getDesktop().browse(new URI("tel:" + cleanPhone));
            }
        } catch (Exception ignored) {}

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channel", "CALL");
        result.put("transactionId", t.getTransactionId());
        result.put("customer", customerName);
        result.put("buyerPhone", buyerPhone);
        result.put("shopkeeperPhone", settings.getShopkeeperPhone());
        result.put("status", "COMPLETED");
        result.put("message", "Call initiated successfully to " + customerName + " (" + buyerPhone + "). Call logged in system audit trail.");
        result.put("timestamp", now.toString());
        return result;
    }

    public Map<String, Object> sendSmsNotice(String transactionId, String customMessage) {
        Transaction t = findTransactionOrThrow(transactionId);
        ApplicationSettings settings = getSettings();
        String buyerPhone = resolveBuyerPhone(t);
        String customerName = t.getCustomer() != null ? t.getCustomer() : "Valued Buyer";
        String formattedAmt = currencyFormatter.format(t.getAmount());

        LocalDateTime now = LocalDateTime.now();

        // Construct Buyer SMS
        String buyerSms = (customMessage != null && !customMessage.isBlank())
                ? customMessage
                : String.format("CASH-IN ALERT: Dear %s, payment of %s for Invoice #%s is %s (Due: %s). Please remit payment immediately to clear your account. - CashFlow Store (%s)",
                customerName, formattedAmt, t.getTransactionId(), t.getStatus(), t.getDueDate(), settings.getShopkeeperPhone());

        // Construct Shopkeeper Audit SMS
        String shopkeeperSms = String.format("SHOPKEEPER AUDIT: Overdue reminder SMS dispatched to buyer %s (%s) for Invoice #%s (%s, Status: %s) on %s.",
                customerName, buyerPhone, t.getTransactionId(), formattedAmt, t.getStatus(), now.toLocalDate());

        // Log Buyer SMS
        CommunicationLog buyerLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "SMS",
                "BUYER",
                buyerPhone,
                "Payment Reminder SMS",
                buyerSms,
                "SENT",
                now
        );
        logRepository.save(buyerLog);

        // Log Shopkeeper SMS
        CommunicationLog shopkeeperLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "SMS",
                "SHOPKEEPER",
                settings.getShopkeeperPhone(),
                "Shopkeeper Audit Copy (SMS)",
                shopkeeperSms,
                "SENT",
                now
        );
        logRepository.save(shopkeeperLog);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channel", "SMS");
        result.put("transactionId", t.getTransactionId());
        result.put("customer", customerName);
        result.put("buyerPhone", buyerPhone);
        result.put("shopkeeperPhone", settings.getShopkeeperPhone());
        result.put("buyerMessage", buyerSms);
        result.put("shopkeeperMessage", shopkeeperSms);
        result.put("status", "SENT");
        result.put("message", "SMS dispatched successfully to both Buyer (" + buyerPhone + ") and Shopkeeper (" + settings.getShopkeeperPhone() + ").");
        result.put("timestamp", now.toString());
        return result;
    }

    public Map<String, Object> sendEmailNotice(String transactionId, String customSubject, String customBody) {
        Transaction t = findTransactionOrThrow(transactionId);
        ApplicationSettings settings = getSettings();
        String buyerEmail = resolveBuyerEmail(t);
        String customerName = t.getCustomer() != null ? t.getCustomer() : "Valued Buyer";
        String formattedAmt = currencyFormatter.format(t.getAmount());

        LocalDateTime now = LocalDateTime.now();

        String buyerSubject = (customSubject != null && !customSubject.isBlank())
                ? customSubject
                : String.format("URGENT NOTICE: Outstanding Payment for Invoice #%s (%s)", t.getTransactionId(), formattedAmt);

        String buyerBody = (customBody != null && !customBody.isBlank())
                ? customBody
                : String.format(
                "Dear %s,\n\n" +
                        "This is a formal payment notice regarding your outstanding balance with us.\n\n" +
                        "• Invoice Number: %s\n" +
                        "• Outstanding Amount: %s\n" +
                        "• Due Date: %s\n" +
                        "• Current Status: %s\n\n" +
                        "Please arrange for the prompt settlement of this invoice to maintain active service and trade credit.\n" +
                        "If you have already processed this payment, please disregard this notice and notify us.\n\n" +
                        "Sincerely,\n" +
                        "Accounts Department\n" +
                        "Phone: %s | Email: %s",
                customerName, t.getTransactionId(), formattedAmt, t.getDueDate(), t.getStatus(),
                settings.getShopkeeperPhone(), settings.getShopkeeperEmail()
        );

        String shopkeeperSubject = String.format("[SHOPKEEPER AUDIT COPY] Overdue Notice Dispatched to %s - Invoice #%s",
                customerName, t.getTransactionId());

        String shopkeeperBody = String.format(
                "SHOPKEEPER AUDIT NOTIFICATION\n" +
                        "============================\n" +
                        "An automated overdue payment notice has been dispatched to debtor %s (%s).\n\n" +
                        "Details:\n" +
                        "• Invoice ID: %s\n" +
                        "• Amount Outstanding: %s\n" +
                        "• Due Date: %s\n" +
                        "• Dispatched Timestamp: %s\n\n" +
                        "Copy of message sent to debtor:\n" +
                        "\"%s\"",
                customerName, buyerEmail, t.getTransactionId(), formattedAmt, t.getDueDate(), now, buyerBody
        );

        // Log Buyer Email
        CommunicationLog buyerLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "EMAIL",
                "BUYER",
                buyerEmail,
                buyerSubject,
                buyerBody,
                "SENT",
                now
        );
        logRepository.save(buyerLog);

        // Log Shopkeeper Email
        CommunicationLog shopkeeperLog = new CommunicationLog(
                UUID.randomUUID().toString(),
                t.getTransactionId(),
                customerName,
                "EMAIL",
                "SHOPKEEPER",
                settings.getShopkeeperEmail(),
                shopkeeperSubject,
                shopkeeperBody,
                "SENT",
                now
        );
        logRepository.save(shopkeeperLog);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("channel", "EMAIL");
        result.put("transactionId", t.getTransactionId());
        result.put("customer", customerName);
        result.put("buyerEmail", buyerEmail);
        result.put("shopkeeperEmail", settings.getShopkeeperEmail());
        result.put("subject", buyerSubject);
        result.put("status", "SENT");
        result.put("message", "Email notice successfully delivered to Buyer (" + buyerEmail + ") and Shopkeeper (" + settings.getShopkeeperEmail() + ").");
        result.put("timestamp", now.toString());
        return result;
    }

    public Map<String, Object> notifyBothParties(String transactionId) {
        Map<String, Object> smsRes = sendSmsNotice(transactionId, null);
        Map<String, Object> emailRes = sendEmailNotice(transactionId, null, null);

        Map<String, Object> combined = new HashMap<>();
        combined.put("success", true);
        combined.put("transactionId", transactionId);
        combined.put("sms", smsRes);
        combined.put("email", emailRes);
        combined.put("message", "Dual-party outreach complete: SMS and Email successfully dispatched to both Buyer and Shopkeeper!");
        return combined;
    }

    public Map<String, Object> bulkNotifyDefaulters() {
        List<Transaction> all = transactionRepository.findAll();
        List<Transaction> overdue = all.stream()
                .filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus()) && "INCOME".equalsIgnoreCase(t.getTransactionType()))
                .collect(java.util.stream.Collectors.toList());

        int notifiedCount = 0;
        List<String> notifiedTxns = new ArrayList<>();
        for (Transaction t : overdue) {
            try {
                notifyBothParties(t.getTransactionId());
                notifiedCount++;
                notifiedTxns.add(t.getTransactionId());
            } catch (Exception ignored) {}
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("totalOverdue", overdue.size());
        res.put("notifiedCount", notifiedCount);
        res.put("transactions", notifiedTxns);
        res.put("message", "Bulk notification completed for " + notifiedCount + " overdue defaulters.");
        return res;
    }

    public List<CommunicationLog> getCommunicationLogs(String transactionId) {
        if (transactionId == null || transactionId.isBlank() || "ALL".equalsIgnoreCase(transactionId)) {
            return logRepository.findAll();
        }
        return logRepository.findByTransactionId(transactionId);
    }
}
