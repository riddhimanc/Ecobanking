package com.ecobank.core.domain;

public record CreateOpportunityDTO(Long customerId, Long productId, String assignedStaff, String status) {

}
