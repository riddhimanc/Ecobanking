package com.ecobank.core.controllers;

import com.ecobank.core.services.*;
import com.ecobank.core.models.Account;
import com.ecobank.core.models.Customer;
import com.ecobank.core.domain.AccountDTO;
import com.ecobank.core.domain.AccountDetailsApiResponse;
import com.ecobank.core.domain.CreateOpportunityDTO;
import com.ecobank.core.domain.OpportunityDTO;
import com.ecobank.core.domain.ProductRepository;
import com.ecobank.core.models.CustomerOpportunity;
import com.ecobank.core.models.products;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Single controller exposing customer/account read endpoints and opportunity
 * CRUD.
 * Assumptions:
 * - CustomerOpportunity has fields: customer (Customer), product (Product),
 * assignedStaff (String), status (String).
 * - Account has fields: accountNo (String), type (String), balance (Double),
 * customer (Customer).
 * - Services are already defined: CustomerServices, AccountServices,
 * CustomerOpportunityServices.
 */
@RestController
@RequestMapping("/api")
// @CrossOrigin(origins = "*") // open for local React dev; tighten in prod
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")

public class EcoBankController {

  private final CustomerServices customerServices;
  private final AccountServices accountServices;
  private final CustomerOpportunityServices opportunityServices;
  private final TransactionServices transactionServices;
  private final ProductRepository productRepository;

  // Status sets used for pending/fulfilled filters
  private static final Set<String> PENDING_STATUSES = Set.of("NEW", "CONTACTED", "PENDING");
  private static final Set<String> FULFILLED_STATUSES = Set.of("WON", "FULFILLED", "CLOSED");

  public EcoBankController(CustomerServices customerServices,
      AccountServices accountServices,
      CustomerOpportunityServices opportunityServices,
      TransactionServices transactionServices,
      ProductRepository productRepository 
    ) {
    this.customerServices = customerServices;
    this.accountServices = accountServices;
    this.opportunityServices = opportunityServices;
    this.transactionServices = transactionServices;
    this.productRepository = productRepository;
  }

  /*
   * ---------------------------------------------------------
   * 0) GET ALL CUSTOMERS (REQUIRED FOR ANGULAR)
   * ---------------------------------------------------------
   */
  @GetMapping("/customers")
  public ResponseEntity<List<Customer>> getAllCustomers() {
    return ResponseEntity.ok(customerServices.getAllCustomers());
  }

  /*
   * ---------------------------------------------------------
   * 1) GET CUSTOMER
   * ---------------------------------------------------------
   */
  @GetMapping("/customers/{customerId}")
  public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId) {
    Customer c = customerServices.getCustomerById(customerId);
    return ResponseEntity.ok(c);
  }

  /*
   * ---------------------------------------------------------
   * 2) GET CUSTOMER-ACCOUNTS
   * ---------------------------------------------------------
   */
  @GetMapping("/customers/{customerId}/accounts")
  public ResponseEntity<List<Account>> getCustomerAccounts(@PathVariable Long customerId) {
    List<Account> accs = accountServices.getAccountsByCustomerId(customerId);
    return ResponseEntity.ok(accs);
  }

  /*
   * ---------------------------------------------------------
   * 3) GET CUSTOMER-ACCT-BALANCES
   * Returns a compact DTO per account.
   * ---------------------------------------------------------
   */
  @GetMapping("/customers/{customerId}/accounts/balances")
  public ResponseEntity<List<AccountDTO>> getCustomerAccountBalances(@PathVariable Long customerId) {
    List<Account> accs = accountServices.getAccountsByCustomerId(customerId);
    List<AccountDTO> out = accs.stream()
        .map(a -> new AccountDTO(a.getId(), a.getAccountNo(), a.getType(),
            Optional.ofNullable(a.getBalance()).orElse(0.0)))
        .collect(Collectors.toList());
    return ResponseEntity.ok(out);
  }

  /*
   * ---------------------------------------------------------
   * 4) ADD OPPORTUNITY
   * ---------------------------------------------------------
   */

  @GetMapping("/opportunities/{id}")
public ResponseEntity<OpportunityDTO> getOpportunityById(@PathVariable Long id) {

    var opp = opportunityServices.getOpportunityById(id);
    if (opp == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
        new OpportunityDTO(
            opp.getId(),
            opp.getCustomer().getId(),
            opp.getProductId(),        // see NOTE below
            null,                      // productName optional
            opp.getAssignedStaff(),
            opp.getStatus()
        )
    );
}

  /*
   * ---------------------------------------------------------
   * 5) ADD OPPORTUNITY
   * ---------------------------------------------------------
   */
  // @PostMapping("/opportunities")
  // public ResponseEntity<CustomerOpportunity> addOpportunity(@Valid @RequestBody CreateOpportunityDTO req) {
  //   CustomerOpportunity created = opportunityServices.createOpportunity(
  //       req.customerId(), req.productId(), req.assignedStaff(), req.status());
  //   return ResponseEntity.status(HttpStatus.CREATED).body(created);
  // }
@PostMapping("/account/{accountNumber}/opportunity")
public ResponseEntity<Void> createOpportunityApi(
        @PathVariable String accountNumber,
        @RequestBody CreateOpportunityDTO req) {

    var account = accountServices.getAccountByAccountNo(accountNumber);
    if (account == null) {
        return ResponseEntity.badRequest().build();
    }

    Long customerId = account.getCustomer().getId();

    opportunityServices.createOpportunity(
        customerId,
        req.productId(),
        req.assignedStaff(),
        req.status()
    );

    return ResponseEntity.ok().build();
}



  /*
   * ---------------------------------------------------------
   * 6) DELETE OPPORTUNITY
   * ---------------------------------------------------------
   */
  @DeleteMapping("/opportunities/{id}")
  public ResponseEntity<Void> deleteOpportunity(@PathVariable Long id) {
    // Add this method in your CustomerOpportunityServices if not present:
    // public void deleteOpportunity(Long id)
    opportunityServices.deleteOpportunity(id);
    return ResponseEntity.noContent().build();
  }

  /*
   * ---------------------------------------------------------
   * 7) UPDATE OPPORTUNITY (staff and/or status)
   * ---------------------------------------------------------
   */
  // @PutMapping("/opportunities/{id}")
  // public ResponseEntity<CustomerOpportunity> updateOpportunity(@PathVariable Long id,
  //     @Valid @RequestBody UpdateOpportunityRequest req) {
  //   CustomerOpportunity updated = opportunityServices.updateOpportunity(id, req.assignedStaff(), req.status());
  //   return ResponseEntity.ok(updated);
  // }

  @PutMapping("/opportunities/{id}")
public ResponseEntity<Void> updateOpportunityApi(
        @PathVariable Long id,
        @RequestBody UpdateOpportunityRequest dto) {

    opportunityServices.updateOpportunity(
        id,
        dto.assignedStaff(),
        dto.status()
    );

    return ResponseEntity.ok().build();
}


  /*
   * ---------------------------------------------------------
   * 8) GET PENDING OPPORTUNITIES (by customer)
   * ---------------------------------------------------------
   */
  @GetMapping("/opportunities/pending")
  public ResponseEntity<List<CustomerOpportunity>> getPendingOpportunities(
      @RequestParam @NotNull @Positive Long customerId) {
    List<CustomerOpportunity> all = opportunityServices.getOpportunitiesByCustomer(customerId);
    List<CustomerOpportunity> pending = all.stream()
        .filter(o -> o.getStatus() != null && PENDING_STATUSES.contains(((String) o.getStatus()).toUpperCase()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(pending);
  }

  /*
   * ---------------------------------------------------------
   * 8) GET FULFILLED OPPORTUNITIES (by customer)
   * ---------------------------------------------------------
   */
  @GetMapping("/opportunities/fulfilled")
  public ResponseEntity<List<CustomerOpportunity>> getFulfilledOpportunities(
      @RequestParam @NotNull @Positive Long customerId) {
    List<CustomerOpportunity> all = opportunityServices.getOpportunitiesByCustomer(customerId);
    List<CustomerOpportunity> fulfilled = all.stream()
        .filter(o -> o.getStatus() != null && FULFILLED_STATUSES.contains(((String) o.getStatus()).toUpperCase()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(fulfilled);
  }

  /*
   * ---------------------------------------------------------
   * 9) GET USER DETAILS
   * ---------------------------------------------------------
   */

  @GetMapping("/api/me")
  public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
    return Map.of(
        "name", user.getAttribute("name"),
        "email", user.getAttribute("email"));
  }

  /*
   * ---------------------------------------------------------
   * 10) GET ALL PRODUCTS
   * ---------------------------------------------------------
   */

  @GetMapping("/account/details")
public ResponseEntity<AccountDetailsApiResponse> getAccountDetailsForAngular(
        @RequestParam String accountNumber) {

    Account account = accountServices.getAccountByAccountNo(accountNumber);
    if (account == null) {
        return ResponseEntity.notFound().build();
    }

    Long customerId = account.getCustomer().getId();

    return ResponseEntity.ok(
    new AccountDetailsApiResponse(
        account,
        transactionServices.getTransactionDTOsByAccountNo(accountNumber),
        opportunityServices.getOpportunitiesByCustomerDTO(customerId)
    )
);
}


@PostMapping("/account/{accountNumber}/opportunities/generate")
public ResponseEntity<Map<String, Object>> generateOpportunitiesApi(
        @PathVariable String accountNumber) {

    var account = accountServices.getAccountByAccountNo(accountNumber);
    if (account == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Account not found"));
    }

    Long customerId = account.getCustomer().getId();

    var created = opportunityServices.generateAndInsertOpportunitiesForCustomer(
        customerId,
        90,
        "AUTO-AI"
    );

    return ResponseEntity.ok(
        Map.of(
            "message", created.isEmpty()
                ? "No new opportunities were generated."
                : created.size() + " opportunities generated.",
            "opportunities", opportunityServices.getOpportunitiesByCustomer(customerId)
        )
    );
}




  @GetMapping("/products")
public ResponseEntity<List<products>> getAllProducts() {
    return ResponseEntity.ok(productRepository.findAll());
}





}
