
package com.ecobank.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "accounts")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String accountNo;

  @NotBlank
  @Column(nullable = false)
  private String type; // e.g., SAVINGS, CURRENT

  @PositiveOrZero
  private Double balance = 0.0;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  @JsonIgnore
  private Customer customer;

  // ----- getters & setters -----
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getAccountNo() { return accountNo; }
  public void setAccountNo(String accountNo) { this.accountNo = accountNo; }

  public String getType() { return type; }
  public void setType(String type) { this.type = type; }

  public Double getBalance() { return balance; }
  public void setBalance(Double balance) { this.balance = balance; }

  public Customer getCustomer() { return customer; }
  public void setCustomer(Customer customer) { this.customer = customer; }
}
