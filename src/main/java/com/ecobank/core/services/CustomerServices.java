package com.ecobank.core.services;


// src/main/java/com/ecobank/core/services/CustomerServices.java

import com.ecobank.core.models.Customer;
import com.ecobank.core.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerServices {

  private final CustomerRepository customers;

  public CustomerServices(CustomerRepository customers) {
    this.customers = customers;
  }

  /** Fetch all customers (read-only) */
  public List<Customer> getAllCustomers() {
    return customers.findAll();
  }

  /** Fetch one customer by id (read-only) */
  public Customer getCustomerById(Long id) {
    return customers.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
  }
}

