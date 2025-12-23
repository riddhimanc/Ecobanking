package com.ecobank.core.domain;



import com.ecobank.core.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

  /** Accounts for a given customer (association on Account.customer) */
  List<Account> findByCustomer_Id(Long customerId);

  /** Lookup by unique account number */
  Account findByAccountNo(String accountNo);
}
