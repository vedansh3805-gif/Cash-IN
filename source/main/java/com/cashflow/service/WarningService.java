package com.cashflow.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.Transaction;
import com.cashflow.model.Warning;
import com.cashflow.repository.TransactionRepository;
import com.cashflow.repository.WarningRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WarningService {

    private final WarningRepository warningRepository;
    private final TransactionRepository transactionRepository;

    public WarningService(WarningRepository warningRepository, TransactionRepository transactionRepository) {
        this.warningRepository = warningRepository;
        this.transactionRepository = transactionRepository;
    }

    public void generateWarnings(String versionId, List<ForecastEntry> forecast, BigDecimal threshold) {
        List<Warning> warnings = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // 1. Minimum Cash Threshold Breach Warning
        if (forecast != null && threshold != null) {
            for (ForecastEntry f : forecast) {
                if (f.getClosingCash().compareTo(threshold) < 0) {
                    BigDecimal deficit = threshold.subtract(f.getClosingCash());
                    String severity = f.getClosingCash().compareTo(BigDecimal.ZERO) < 0 ? "CRITICAL" : "WARNING";
                    String message = String.format("Projected cash falls below threshold of ₹%s on %s. Projected minimum: ₹%s (Deficit: ₹%s).",
                            threshold.toPlainString(), f.getForecastDate(), f.getClosingCash().toPlainString(), deficit.toPlainString());

                    Warning w = new Warning(
                            UUID.randomUUID().toString(),
                            versionId,
                            "LOW_PROJECTED_CASH",
                            severity,
                            now,
                            f.getForecastDate(),
                            message,
                            "ACTIVE"
                    );
                    warnings.add(w);
                    break; // report earliest breach
                }
            }
        }

        // 2. Overdue Receivables Warning
        List<Transaction> transactions = transactionRepository.findAllByVersionId(versionId);
        BigDecimal overdueTotal = BigDecimal.ZERO;
        int overdueCount = 0;
        List<String> overdueCustomers = new ArrayList<>();

        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getTransactionType()) && "OVERDUE".equalsIgnoreCase(t.getStatus())) {
                overdueTotal = overdueTotal.add(t.getAmount());
                overdueCount++;
                if (t.getCustomer() != null && !t.getCustomer().isBlank() && !overdueCustomers.contains(t.getCustomer())) {
                    overdueCustomers.add(t.getCustomer());
                }
            }
        }

        if (overdueTotal.compareTo(BigDecimal.ZERO) > 0) {
            String severity = overdueTotal.compareTo(new BigDecimal("100000")) > 0 ? "WARNING" : "ATTENTION";
            String message = String.format("Overdue receivables detected totaling ₹%s across %d invoice(s). Customers: %s.",
                    overdueTotal.toPlainString(), overdueCount, String.join(", ", overdueCustomers));

            Warning w = new Warning(
                    UUID.randomUUID().toString(),
                    versionId,
                    "OVERDUE_RECEIVABLE",
                    severity,
                    now,
                    now,
                    message,
                    "ACTIVE"
            );
            warnings.add(w);
        }

        // 3. Upcoming Large Expenses
        BigDecimal largeExpenseThreshold = new BigDecimal("75000");
        for (Transaction t : transactions) {
            if ("EXPENSE".equalsIgnoreCase(t.getTransactionType()) &&
                    "PENDING".equalsIgnoreCase(t.getStatus()) &&
                    t.getAmount().compareTo(largeExpenseThreshold) >= 0) {

                String message = String.format("Upcoming significant expense of ₹%s for %s (%s) due on %s.",
                        t.getAmount().toPlainString(), t.getCategory(), t.getDescription(), t.getDueDate());

                Warning w = new Warning(
                        UUID.randomUUID().toString(),
                        versionId,
                        "LARGE_UPCOMING_EXPENSE",
                        "INFO",
                        now,
                        t.getDueDate(),
                        message,
                        "ACTIVE"
                );
                warnings.add(w);
            }
        }

        if (!warnings.isEmpty()) {
            warningRepository.saveAll(warnings);
        }
    }
}
