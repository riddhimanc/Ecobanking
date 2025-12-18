package com.ecobank.core.models;



import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

@Entity
public class transactions {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "account_id")
  private Account account;

  @NotNull private Double amount; // positive for credit, negative for debit
  @NotNull private LocalDateTime createdAt = LocalDateTime.now();

  @NotBlank private String description;

  // getters/setters
}
