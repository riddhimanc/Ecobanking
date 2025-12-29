
package com.ecobank.core.services;

import com.ecobank.core.models.Transactions;
import com.ecobank.core.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServices {

    private final TransactionRepository transactionRepository;

    public TransactionServices(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transactions> getTransactionsByAccountNo(String accountNo) {
        return transactionRepository.findByAccount_AccountNoOrderByCreatedAtDesc(accountNo);
    }

    // If your Account field is accountNumber:
    // public List<Transactions> getTransactionsByAccountNumber(String accountNumber) {
    //     return transactionRepository.findByAccount_AccountNumberOrderByCreatedAtDesc(accountNumber);
    // }
}
