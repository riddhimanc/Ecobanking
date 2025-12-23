
package com.ecobank.core.domain;

import com.ecobank.core.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Customer entity.
 * * Provides basic CRUD and query methods via Spring Data JPA.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // You can add custom queries if needed, e.g.:
    // Optional<Customer> findByEmail(String email);
}