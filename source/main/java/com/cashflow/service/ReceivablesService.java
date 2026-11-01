package com.cashflow.service;

import com.cashflow.model.Customer;
import com.cashflow.model.Transaction;
import com.cashflow.repository.CustomerRepository;
import com.cashflow.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReceivablesService {
    
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public ReceivablesService(TransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    public void updateCustomerMetrics(String versionId) {
        List<Transaction> transactions = transactionRepository.findAllByVersionId(versionId);
        Map<String, Customer> map = new HashMap<>();
        Map<String, Long> totalDelayDays = new HashMap<>();
        Map<String, Integer> delayCounts = new HashMap<>();
        
        for (Transaction t : transactions) {
            if ("INCOME".equalsIgnoreCase(t.getTransactionType()) && t.getCustomer() != null && !t.getCustomer().isBlank()) {
                Customer c = map.computeIfAbsent(t.getCustomer(), k -> {
                    Customer cust = new Customer(UUID.randomUUID().toString(), k, 0.0, BigDecimal.ZERO);
                    cust.setPhone(t.getCustomerPhone());
                    cust.setEmail(t.getCustomerEmail());
                    return cust;
                });
                
                if ((c.getPhone() == null || c.getPhone().isBlank()) && t.getCustomerPhone() != null && !t.getCustomerPhone().isBlank()) {
                    c.setPhone(t.getCustomerPhone());
                }
                if ((c.getEmail() == null || c.getEmail().isBlank()) && t.getCustomerEmail() != null && !t.getCustomerEmail().isBlank()) {
                    c.setEmail(t.getCustomerEmail());
                }
                
                if ("PENDING".equalsIgnoreCase(t.getStatus()) || "OVERDUE".equalsIgnoreCase(t.getStatus())) {
                    c.setTotalOutstanding(c.getTotalOutstanding().add(t.getAmount()));
                }

                // Check payment delay history
                if (t.getDueDate() != null && t.getPaymentDate() != null) {
                    long delay = ChronoUnit.DAYS.between(t.getDueDate(), t.getPaymentDate());
                    if (delay > 0) {
                        totalDelayDays.put(t.getCustomer(), totalDelayDays.getOrDefault(t.getCustomer(), 0L) + delay);
                        delayCounts.put(t.getCustomer(), delayCounts.getOrDefault(t.getCustomer(), 0) + 1);
                    }
                } else if ("OVERDUE".equalsIgnoreCase(t.getStatus()) && t.getDueDate() != null) {
                    long delay = ChronoUnit.DAYS.between(t.getDueDate(), LocalDate.now());
                    if (delay > 0) {
                        totalDelayDays.put(t.getCustomer(), totalDelayDays.getOrDefault(t.getCustomer(), 0L) + delay);
                        delayCounts.put(t.getCustomer(), delayCounts.getOrDefault(t.getCustomer(), 0) + 1);
                    }
                }
            }
        }

        for (Map.Entry<String, Customer> entry : map.entrySet()) {
            String custName = entry.getKey();
            Customer c = entry.getValue();
            if (delayCounts.containsKey(custName) && delayCounts.get(custName) > 0) {
                double avg = (double) totalDelayDays.get(custName) / delayCounts.get(custName);
                c.setAveragePaymentDelay(avg);
            }
        }
        
        customerRepository.deleteAll();
        for (Customer c : map.values()) {
            customerRepository.save(c);
        }
    }
}
