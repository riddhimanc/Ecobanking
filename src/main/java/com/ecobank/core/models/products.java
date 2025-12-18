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

  // getters/setters
}

