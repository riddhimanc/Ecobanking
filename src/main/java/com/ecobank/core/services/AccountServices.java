package com.ecobank.core.services;


import com.ecobank.core.domain.AccountRepository;

// src/main/java/com/ecobank/core/application/AccountServices.java


import com.ecobank.core.models.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountServices {

  private final AccountRepository accounts;

  public AccountServices(AccountRepository accounts) {
    this.accounts = accounts;
  }

  /** Fetch accounts for a specific customer (read-only) */
  public List<Account> getAccountsByCustomerId(Long customerId) {
    return accounts.findByCustomer_Id(customerId);
  }

  /** Fetch a single account by id (read-only) */
  public Account getAccountById(Long id) {
    return accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
  }

  /** Fetch a single account by account number (read-only) */
  public Account getAccountByAccountNo(String accountNo) {
    Account acc = accounts.findByAccountNo(accountNo);
    if (acc == null) throw new IllegalArgumentException("Account not found: " + accountNo);
    return acc;
  }
}

