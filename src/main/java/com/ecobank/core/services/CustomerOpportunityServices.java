
package com.ecobank.core.services;

import com.ecobank.core.domain.CustomerOpportunityRepository;
import com.ecobank.core.domain.CustomerRepository;
import com.ecobank.core.models.Customer;
import com.ecobank.core.models.CustomerOpportunity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerOpportunityServices {

  private final CustomerOpportunityRepository opps;
  private final CustomerRepository customers;

  public CustomerOpportunityServices(CustomerOpportunityRepository opps, CustomerRepository customers) {
    this.opps = opps;
    this.customers = customers;
  }

  /* -------- SELECT -------- */
  @Transactional(readOnly = true)
  public List<CustomerOpportunity> getOpportunitiesByCustomer(Long customerId) {
    return opps.findByCustomer_Id(customerId);
  }

  @Transactional(readOnly = true)
  public CustomerOpportunity getOpportunityById(Long id) {
    return opps.findById(id).orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));
  }

  /* -------- INSERT -------- */
  @Transactional
  public CustomerOpportunity createOpportunity(Long customerId, Long productId, String assignedStaff, String status) {
    Customer customer = customers.findById(customerId)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

    CustomerOpportunity co = new CustomerOpportunity();
    co.setCustomer(customer);
    co.setProductId(productId);       // using FK field for Product for now
    co.setAssignedStaff(assignedStaff);
    co.setStatus(status);
    return opps.save(co);
  }

  /* -------- UPDATE -------- */
  @Transactional
  public CustomerOpportunity updateOpportunity(Long id, String assignedStaff, String status) {
    CustomerOpportunity co = opps.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + id));

    if (assignedStaff != null && !assignedStaff.isBlank()) co.setAssignedStaff(assignedStaff);
    if (status != null && !status.isBlank()) co.setStatus(status);
    return opps.save(co);
  }

  /* -------- DELETE -------- */
  @Transactional
  public void deleteOpportunity(Long id) {
    opps.deleteById(id);
  }

  /* -------- Filters -------- */
  @Transactional(readOnly = true)
  public List<CustomerOpportunity> getPending(Long customerId) {
    return opps.findByCustomer_IdAndStatusIn(customerId, List.of("NEW", "CONTACTED", "PENDING"));
  }

  @Transactional(readOnly = true)
  public List<CustomerOpportunity> getFulfilled(Long customerId) {
    return opps.findByCustomer_IdAndStatusIn(customerId, List.of("WON", "FULFILLED", "CLOSED"));
  }
}

