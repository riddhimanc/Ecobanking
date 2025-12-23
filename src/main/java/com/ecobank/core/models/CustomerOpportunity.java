
package com.ecobank.core.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "customer_opportunities")
public class CustomerOpportunity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;   // <-- correct association to Customer entity

  // Using FK only for Product to avoid missing Product class for now.
  @NotNull
  @Column(name = "product_id", nullable = false)
  private Long productId;

  @NotBlank
  @Column(nullable = false)
  private String assignedStaff;

  @NotBlank
  @Column(nullable = false)
  private String status; // e.g., NEW, CONTACTED, WON, LOST, FULFILLED

  // ----- getters & setters -----
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Customer getCustomer() { return customer; }
  public void setCustomer(Customer customer) { this.customer = customer; }

  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }

  public String getAssignedStaff() { return assignedStaff; }
  public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}

