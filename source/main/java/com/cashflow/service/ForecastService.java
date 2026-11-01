package com.cashflow.service;

import com.cashflow.model.ForecastEntry;
import com.cashflow.model.RecurringPayment;
import com.cashflow.model.Transaction;
import com.cashflow.repository.ForecastRepository;
import com.cashflow.repository.RecurringPaymentRepository;
import com.cashflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ForecastService {

    private final TransactionRepository transactionRepository;
    private final RecurringPaymentRepository recurringPaymentRepository;
    private final ForecastRepository forecastRepository;

    public ForecastService(TransactionRepository transactionRepository,
                           RecurringPaymentRepository recurringPaymentRepository,
                           ForecastRepository forecastRepository) {
        this.transactionRepository = transactionRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.forecastRepository = forecastRepository;
    }

    public List<ForecastEntry> generateForecast(String versionId, int days) {
        List<Transaction> allTransactions = transactionRepository.findAllByVersionId(versionId);

        // Current cash = realized paid income minus realized paid expense
        BigDecimal currentCash = BigDecimal.ZERO;
        LocalDate maxPaidDate = null;
        LocalDate maxTxnDate = null;
        for (Transaction t : allTransactions) {
            if ("PAID".equalsIgnoreCase(t.getStatus())) {
                if ("INCOME".equalsIgnoreCase(t.getTransactionType())) {
                    currentCash = currentCash.add(t.getAmount());
                } else if ("EXPENSE".equalsIgnoreCase(t.getTransactionType())) {
                    currentCash = currentCash.subtract(t.getAmount());
                }
                LocalDate pDate = t.getPaymentDate() != null ? t.getPaymentDate() : t.getTransactionDate();
                if (pDate != null && (maxPaidDate == null || pDate.isAfter(maxPaidDate))) {
                    maxPaidDate = pDate;
                }
            }
            LocalDate tDate = t.getTransactionDate();
            if (tDate != null && (maxTxnDate == null || tDate.isAfter(maxTxnDate))) {
                maxTxnDate = tDate;
            }
            LocalDate dDate = t.getDueDate();
            if (dDate != null && (maxTxnDate == null || dDate.isAfter(maxTxnDate))) {
                maxTxnDate = dDate;
            }
        }

        // Anchor date: snapshot reference date from which future days are projected
        LocalDate anchorDate = LocalDate.now();
        if (maxPaidDate != null && maxPaidDate.isBefore(anchorDate.minusDays(30))) {
            anchorDate = maxPaidDate;
        } else if (maxPaidDate == null && maxTxnDate != null && maxTxnDate.isBefore(anchorDate.minusDays(30))) {
            anchorDate = maxTxnDate.minusDays(30);
        }

        List<Transaction> pendingIncomes = allTransactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getTransactionType()))
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()) || "OVERDUE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        List<Transaction> pendingExpenses = allTransactions.stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getTransactionType()))
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()) || "OVERDUE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        List<RecurringPayment> recurrings = recurringPaymentRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .collect(Collectors.toList());

        List<ForecastEntry> entries = new ArrayList<>();
        BigDecimal opening = currentCash;

        for (int i = 0; i < days; i++) {
            LocalDate targetDate = anchorDate.plusDays(i);
            BigDecimal dailyInflow = BigDecimal.ZERO;
            BigDecimal dailyOutflow = BigDecimal.ZERO;

            // Inflows from pending/overdue
            for (Transaction t : pendingIncomes) {
                LocalDate due = t.getDueDate() != null ? t.getDueDate() : t.getTransactionDate();
                if (due == null) continue;
                if ("OVERDUE".equalsIgnoreCase(t.getStatus()) || due.isBefore(anchorDate)) {
                    // Spread overdue recovery across first 7 days
                    if (i == (Math.abs(t.getTransactionId().hashCode()) % 7)) {
                        dailyInflow = dailyInflow.add(t.getAmount());
                    }
                } else if (targetDate.equals(due)) {
                    dailyInflow = dailyInflow.add(t.getAmount());
                }
            }

            // Outflows from pending/overdue expenses
            for (Transaction t : pendingExpenses) {
                LocalDate due = t.getDueDate() != null ? t.getDueDate() : t.getTransactionDate();
                if (due == null) continue;
                if (targetDate.equals(due)) {
                    dailyOutflow = dailyOutflow.add(t.getAmount());
                } else if (due.isBefore(anchorDate) && i == 0) {
                    dailyOutflow = dailyOutflow.add(t.getAmount());
                }
            }

            // Recurring payments
            for (RecurringPayment r : recurrings) {
                String freq = r.getFrequency() != null ? r.getFrequency().toUpperCase() : "MONTHLY";
                int interval = "DAILY".equals(freq) ? 1 : ("WEEKLY".equals(freq) ? 7 : 30);

                if (r.getNextDueDate() != null) {
                    LocalDate nextDue = r.getNextDueDate();
                    if (targetDate.equals(nextDue)) {
                        dailyOutflow = dailyOutflow.add(r.getAmount());
                    } else if (targetDate.isAfter(nextDue)) {
                        long daysSince = java.time.temporal.ChronoUnit.DAYS.between(nextDue, targetDate);
                        if (daysSince > 0 && daysSince % interval == 0) {
                            dailyOutflow = dailyOutflow.add(r.getAmount());
                        }
                    } else if (nextDue.isAfter(anchorDate.plusDays(days)) && i > 0 && i % interval == 0) {
                        // If next due date is far in the future, schedule periodically from anchor date
                        dailyOutflow = dailyOutflow.add(r.getAmount());
                    }
                } else if (i > 0 && i % interval == 0) {
                    dailyOutflow = dailyOutflow.add(r.getAmount());
                }
            }

            BigDecimal closing = opening.add(dailyInflow).subtract(dailyOutflow);

            ForecastEntry entry = new ForecastEntry(
                    UUID.randomUUID().toString(),
                    versionId,
                    targetDate,
                    opening,
                    dailyInflow,
                    dailyOutflow,
                    closing
            );
            entries.add(entry);

            opening = closing;
        }

        forecastRepository.saveAll(entries);
        return entries;
    }
}
