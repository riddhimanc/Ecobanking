package com.ecobank.core.domain;


// src/main/java/com/ecobank/core/domain/ProductRepository.java


import com.ecobank.core.models.products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<products, Long> {
    Optional<products> findByProductCodeIgnoreCase(String productCode);
    Optional<products> findByNameIgnoreCase(String name);
    List<products> findByNameContainingIgnoreCase(String needle);
}

