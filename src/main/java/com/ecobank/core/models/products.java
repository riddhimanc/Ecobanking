package com.ecobank.core.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class products {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Column(unique = true)
  private String productCode;

  @NotBlank
  private String name;

  private String details;

  // Existing getter
  public Long getId() {
    return id;
  }

  // ADD THESE GETTERS
  public String getProductCode() {
    return productCode;
  }

  public String getName() {
    return name;
  }

  public String getDetails() {
    return details;
  }

  // Setters (Required for form binding if you save Product objects)
  public void setProductCode(String productCode) { this.productCode = productCode; }
  public void setName(String name) { this.name = name; }
  public void setDetails(String details) { this.details = details; }
}