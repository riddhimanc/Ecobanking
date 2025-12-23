
// src/main/java/com/ecobank/core/controllers/UiController.java
package com.ecobank.core.controllers;

import com.ecobank.core.services.AccountServices;
import com.ecobank.core.services.CustomerOpportunityServices;
import com.ecobank.core.services.CustomerServices;
import com.ecobank.core.services.TransactionServices;
import com.ecobank.core.domain.AiOpportunitySuggestionDTO;
import com.ecobank.core.domain.ProductRepository;
import com.ecobank.core.services.CustomerOpportunityServices;

import com.ecobank.core.models.Account;
import com.ecobank.core.models.Customer;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ui")
public class UiController {

    private final CustomerServices customerServices;
    private final AccountServices accountServices;
    private final CustomerOpportunityServices opportunityServices;
    private final TransactionServices transactionServices;
    private final CustomerOpportunityServices aiOpportunityService; 
    private final ProductRepository productRepository;

    public UiController(CustomerServices customerServices,
                        AccountServices accountServices,
                        CustomerOpportunityServices opportunityServices,
                        TransactionServices transactionServices,
                        CustomerOpportunityServices aiOpportunityService,
                        ProductRepository productRepository) { 
        this.customerServices = customerServices;
        this.accountServices = accountServices;
        this.opportunityServices = opportunityServices;
        this.transactionServices = transactionServices;
        this.aiOpportunityService = aiOpportunityService;
        this.productRepository = productRepository;
    }

//Google Login integration
@GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }



@GetMapping
public String home(@RequestParam(defaultValue = "0") int page, Model model) {
    int pageSize = 10;
    // Get the Page object from your service
    org.springframework.data.domain.Page<Customer> customerPage = customerServices.getAllCustomersPaged(page, pageSize);
    
    // Add the actual list of customers to the "customers" attribute
    model.addAttribute("customers", customerPage.getContent()); 
    
    // Add pagination metadata
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", customerPage.getTotalPages());
    model.addAttribute("totalItems", customerPage.getTotalElements());
    
    return "home"; // templates/home.html
}


  /* ---------------------------------------------------------
   * 1) GET CUSTOMER
   * --------------------------------------------------------- */
  @GetMapping("/customer/{customerId}")
    public String customerDashboard(@PathVariable Long customerId, Model model) {
    Customer c = customerServices.getCustomerById(customerId);
    List<Account> accounts = accountServices.getAccountsByCustomerId(customerId);
    model.addAttribute("customer", c);
    model.addAttribute("accounts", accounts);
    return "customer-dashboard";
  }

  /* ---------------------------------------------------------
   * 2) GET CUSTOMER-ACCOUNTS
   * --------------------------------------------------------- */

@GetMapping("/customer/{customerId}/accounts")
public String customerAccounts(@PathVariable Long customerId, Model model) {
    var customer = customerServices.getCustomerById(customerId);
    var accounts = accountServices.getAccountsByCustomerId(customerId);
    model.addAttribute("customer", customer);
    model.addAttribute("accounts", accounts);
    return "customer-accounts"; // create this template
}



    @GetMapping("/account/{accountNumber}")
    public String accountDetails(@PathVariable String accountNumber, Model model) {
        Account account = accountServices.getAccountByAccountNo(accountNumber); // uses your service
        if (account == null) {
            model.addAttribute("error", "Account not found: " + accountNumber);
            return "account-details";
        }
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactionServices.getTransactionsByAccountNo(accountNumber));
        model.addAttribute("opportunities",
                opportunityServices.getOpportunitiesByCustomer(account.getCustomer().getId()));
        return "account-details";
    }


@PostMapping("/account/{accountNumber}/opportunities/generate")
public String generateOpportunities(@PathVariable String accountNumber, Model model) {
    var account = accountServices.getAccountByAccountNo(accountNumber);
    if (account == null) {
        model.addAttribute("error", "Account not found: " + accountNumber);
        return "account-details";
    }
    Long customerId = account.getCustomer().getId();

    var created = opportunityServices.generateAndInsertOpportunitiesForCustomer(
            customerId,
            /* lookbackDays */ 90,
            /* defaultAssignedStaff */ "AUTO-AI"
    );

    model.addAttribute("account", account);
    model.addAttribute("transactions", transactionServices.getTransactionsByAccountNo(accountNumber));
    model.addAttribute("opportunities", opportunityServices.getOpportunitiesByCustomer(customerId));
    model.addAttribute("message", created.isEmpty()
            ? "No new opportunities were generated."
            : (created.size() + " opportunities generated."));
    return "account-details";
}


@GetMapping("/account/{accountNumber}/opportunity/new")
public String showCreateForm(@PathVariable String accountNumber, Model model) {
    model.addAttribute("accountNumber", accountNumber);
    model.addAttribute("opportunity", new com.ecobank.core.models.CustomerOpportunity());
    model.addAttribute("allProducts", productRepository.findAll());
    return "opportunity-form";
}


@GetMapping("/account/{accountNumber}/opportunity/edit/{oppId}")
public String showEditForm(@PathVariable String accountNumber, @PathVariable Long oppId, Model model) {
    var opp = opportunityServices.getOpportunityById(oppId);
    model.addAttribute("accountNumber", accountNumber);
    model.addAttribute("opportunity", opp);
    model.addAttribute("allProducts", productRepository.findAll());
    return "opportunity-form";
}

@PostMapping("/account/{accountNumber}/opportunity/save")
public String saveOpportunity(@PathVariable String accountNumber,
                              @RequestParam(required = false) Long id,
                              @RequestParam Long productId,
                              @RequestParam String assignedStaff,
                              @RequestParam String status) {
    
    var account = accountServices.getAccountByAccountNo(accountNumber);
    
    if (id == null) {
        // Create new
        opportunityServices.createOpportunity(account.getCustomer().getId(), productId, assignedStaff, status);
    } else {
        // Update existing
        opportunityServices.updateOpportunity(id, assignedStaff, status);
        // Note: If you need to update productId on an existing record, 
        // you may need to add a specific method in CustomerOpportunityServices
    }
    
    return "redirect:/ui/account/" + accountNumber;
}

    // --- helper methods (implement according to your domain) ---

    private String determineAssignedStaff(Account account) {
        // Example: return the RM/relationship manager code from account or customer linkage
        // Fallback: "AUTO-AI"
        return "AUTO-AI";
    }

    private Long resolveProductIdByCodeOrTitle(String productCode, String title) {
        // TODO: Look up productId using repositories by code or normalized title
        return null;
    }
}
