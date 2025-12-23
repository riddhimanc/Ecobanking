package com.ecobank.core.domain;

public record OpportunityDTO(Long id, Long customerId, Long productId, String productName, String assignedStaff, String status) {

}
