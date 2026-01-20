package com.ecobank.core.domain;

public record UpdateOpportunityRequest(
    String assignedStaff,
    String status
) {}

