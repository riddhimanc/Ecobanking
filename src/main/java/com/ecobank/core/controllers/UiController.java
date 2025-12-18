package com.ecobank.core.controllers;



import com.ecobank.core.services.AccountServices;
import com.ecobank.core.services.CustomerOpportunityServices;
import com.ecobank.core.services.CustomerServices;
import com.ecobank.core.models.CustomerOpportunity;
import com.ecobank.core.models.Customer;
import com.ecobank.core.models.Account;

import jakarta.validation.constraints.NotNull;
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

  public UiController(CustomerServices customerServices,
                      AccountServices accountServices,
                      CustomerOpportunityServices opportunityServices) {
    this.customerServices = customerServices;
    this.accountServices = accountServices;
    this.opportunityServices = opportunityServices;
  }

  /** Home: choose a customer */
  @GetMapping
  public String home(Model model) {
    List<Customer> customers = customerServices.getAllCustomers();
    model.addAttribute("customers", customers);
    return "home"; // templates/home.html
  }

  /** Dashboard for a given customer */
  @GetMapping("/customer/{customerId}")
  public String customerDashboard(@PathVariable Long customerId, Model model) {
    Customer c = customerServices.getCustomerById(customerId);
    List<Account> accounts = accountServices.getAccountsByCustomerId(customerId);

    List<CustomerOpportunity> pending = opportunityServices.getPending(customerId);
    List<CustomerOpportunity> fulfilled = opportunityServices.getFulfilled(customerId);

    model.addAttribute("customer", c);
    model.addAttribute("accounts", accounts);
    model.addAttribute("pendingOpps", pending);
    model.addAttribute("fulfilledOpps", fulfilled);

    // backing objects for forms
    model.addAttribute("newOpp", new OpportunityForm(customerId));
    model.addAttribute("updateOpp", new UpdateOpportunityForm());

    return "customer-dashboard"; // templates/customer-dashboard.html
  }

  /** Add Opportunity (POST from Thymeleaf form) */
  @PostMapping("/opportunities")
  public String addOpportunity(@ModelAttribute OpportunityForm form) {
    opportunityServices.createOpportunity(form.getCustomerId(), form.getProductId(),
        form.getAssignedStaff(), form.getStatus());
    return "redirect:/ui/customer/" + form.getCustomerId();
  }

  /** Update Opportunity */
  @PostMapping("/opportunities/{id}/update")
  public String updateOpportunity(@PathVariable Long id, @ModelAttribute UpdateOpportunityForm form,
                                  @RequestParam Long customerId) {
    opportunityServices.updateOpportunity(id, form.getAssignedStaff(), form.getStatus());
    return "redirect:/ui/customer/" + customerId;
  }

  /** Delete Opportunity */
  @PostMapping("/opportunities/{id}/delete")
  public String deleteOpportunity(@PathVariable Long id, @RequestParam Long customerId) {
    opportunityServices.deleteOpportunity(id);
    return "redirect:/ui/customer/" + customerId;
  }

  /* ------------ Simple form DTOs for Thymeleaf binding ------------ */

  public static class OpportunityForm {
    private Long customerId;
    private Long productId;
    private String assignedStaff;
    private String status;

    public OpportunityForm() {}
    public OpportunityForm(Long customerId) { this.customerId = customerId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
  }

  public static class UpdateOpportunityForm {
    private String assignedStaff;
    private String status;
    public String getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
  }
}
