
package com.ecobank.core.domain;

import com.ecobank.core.models.CustomerOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOpportunityRepository extends JpaRepository<CustomerOpportunity, Long> {

  /** All opportunities for a customer (association on CustomerOpportunity.customer) */
  List<CustomerOpportunity> findByCustomer_Id(Long customerId);

  /** Optional: filter by status (case-insensitive) */
  List<CustomerOpportunity> findByCustomer_IdAndStatusIgnoreCase(Long customerId, String status);

  /** Optional: bulk filter by statuses */
  List<CustomerOpportunity> findByCustomer_IdAndStatusIn(Long customerId, List<String> statuses);
}
