
package com.ecobank.core.domain;

import com.ecobank.core.models.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {

    // If Account has a field accountNo, this derived query will work:
    List<Transactions> findByAccount_AccountNoOrderByCreatedAtDesc(String accountNo);

    // If Account uses accountNumber instead, change the method name accordingly:
    // List<Transactions> findByAccount_AccountNumberOrderByCreatedAtDesc(String accountNumber);
}
