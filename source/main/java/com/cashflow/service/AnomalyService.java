package com.cashflow.service;

import com.cashflow.model.Anomaly;
import com.cashflow.model.Transaction;
import com.cashflow.repository.AnomalyRepository;
import com.cashflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnomalyService {

    private final AnomalyRepository anomalyRepository;
    private final TransactionRepository transactionRepository;

    public AnomalyService(AnomalyRepository anomalyRepository, TransactionRepository transactionRepository) {
        this.anomalyRepository = anomalyRepository;
        this.transactionRepository = transactionRepository;
    }

    public void detectAnomalies(String versionId) {
        List<Transaction> transactions = transactionRepository.findAllByVersionId(versionId);
        if (transactions.isEmpty()) return;
        
        List<Anomaly> anomalies = new ArrayList<>();
        
        List<Transaction> expenses = transactions.stream().filter(t -> "EXPENSE".equals(t.getTransactionType())).collect(Collectors.toList());
        if (!expenses.isEmpty()) {
            BigDecimal totalExp = expenses.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgExp = totalExp.divide(new BigDecimal(expenses.size()), 2, RoundingMode.HALF_UP);
            
            for (Transaction t : expenses) {
                if (t.getAmount().compareTo(avgExp.multiply(new BigDecimal("3.0"))) > 0) {
                    Anomaly a = new Anomaly(UUID.randomUUID().toString(), versionId, t.getTransactionId(), "LARGE_EXPENSE", "WARNING", avgExp, t.getAmount(), "Expense is more than 3x the average.");
                    anomalies.add(a);
                }
            }
        }
        
        if (!anomalies.isEmpty()) {
            anomalyRepository.saveAll(anomalies);
        }
    }
}
