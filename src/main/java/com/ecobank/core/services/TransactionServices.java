
package com.ecobank.core.services;

import com.ecobank.core.models.Transactions;
import com.ecobank.core.domain.TransactionDTO;
import com.ecobank.core.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServices {

    private final TransactionRepository transactionRepository;

    public TransactionServices(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // DTO version (for Angular / REST)
    public List<TransactionDTO> getTransactionDTOsByAccountNo(String accountNo) {
        return transactionRepository.findByAccount_AccountNoOrderByCreatedAtDesc(accountNo)
            .stream()
            .map(t -> new TransactionDTO(
                t.getId(),
                t.getAccount().getAccountNo(),
                t.getAmount(),
                t.getDescription(),
                t.getCreatedAt().toString()
            ))
            .toList();
    }

    // ENTITY version (for Thymeleaf)
    public List<Transactions> getTransactionsByAccountNo(String accountNo) {
        return transactionRepository.findByAccount_AccountNoOrderByCreatedAtDesc(accountNo);
    }

    // If your Account field is accountNumber:
    // public List<Transactions> getTransactionsByAccountNumber(String accountNumber) {
    //     return transactionRepository.findByAccount_AccountNumberOrderByCreatedAtDesc(accountNumber);
    // }
}
