
package com.ecobank.core.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String name;

  private String address;

  @PositiveOrZero
  private Double income;

  @Email
  private String email;

  @Pattern(regexp = "^[0-9\\-+() ]{7,20}$")
  private String phone;

  // Bidirectional link to accounts (optional)
  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Account> accounts = new ArrayList<>();

  // Bidirectional link to opportunities (optional)
  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CustomerOpportunity> opportunities = new ArrayList<>();

  // ----- getters & setters -----
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public Double getIncome() { return income; }
  public void setIncome(Double income) { this.income = income; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public List<Account> getAccounts() { return accounts; }
  public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

  public List<CustomerOpportunity> getOpportunities() { return opportunities; }
  public void setOpportunities(List<CustomerOpportunity> opportunities) { this.opportunities = opportunities; }
}
