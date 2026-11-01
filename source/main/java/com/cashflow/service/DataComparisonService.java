package com.cashflow.service;

import com.cashflow.model.DataChange;
import com.cashflow.model.Transaction;
import com.cashflow.repository.DataChangeRepository;
import com.cashflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataComparisonService {

    private final TransactionRepository transactionRepository;
    private final DataChangeRepository dataChangeRepository;

    public DataComparisonService(TransactionRepository transactionRepository, DataChangeRepository dataChangeRepository) {
        this.transactionRepository = transactionRepository;
        this.dataChangeRepository = dataChangeRepository;
    }

    public void compareAndSaveChanges(String prevVersionId, String newVersionId, List<Transaction> newTransactions) {
        List<Transaction> oldTransactions = transactionRepository.findAllByVersionId(prevVersionId);
        Map<String, Transaction> oldMap = oldTransactions.stream().collect(Collectors.toMap(Transaction::getTransactionId, t -> t));
        
        List<DataChange> changes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Transaction newT : newTransactions) {
            Transaction oldT = oldMap.remove(newT.getTransactionId());
            if (oldT == null) {
                changes.add(new DataChange(UUID.randomUUID().toString(), newVersionId, newT.getTransactionId(), "ADDED", null, null, null, now));
            } else {
                if (oldT.getAmount().compareTo(newT.getAmount()) != 0) {
                    changes.add(new DataChange(UUID.randomUUID().toString(), newVersionId, newT.getTransactionId(), "MODIFIED", "amount", oldT.getAmount().toString(), newT.getAmount().toString(), now));
                }
                if (!oldT.getStatus().equals(newT.getStatus())) {
                    changes.add(new DataChange(UUID.randomUUID().toString(), newVersionId, newT.getTransactionId(), "MODIFIED", "status", oldT.getStatus(), newT.getStatus(), now));
                }
            }
        }
        
        for (String removedId : oldMap.keySet()) {
            changes.add(new DataChange(UUID.randomUUID().toString(), newVersionId, removedId, "REMOVED", null, null, null, now));
        }
        
        if (!changes.isEmpty()) {
            dataChangeRepository.saveAll(changes);
        }
    }
}
