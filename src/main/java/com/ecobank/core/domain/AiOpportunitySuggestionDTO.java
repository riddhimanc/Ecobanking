package com.ecobank.core.domain;


// com.ecobank.core.domain.AiOpportunitySuggestionDTO.java


public record AiOpportunitySuggestionDTO(
  Long productId,           // existing Product id (nullable if new idea)
  String productCode,       // optional hint used for matching
  String title,             // short headline
  String rationale,         // why this is relevant for the customer
  String recommendedAction, // e.g., "call customer", "send email offer"
  String segment,           // e.g., "Home Loan", "Wealth", "Insurance"
  double confidence,        // 0..1 score from model
  String constraints        // disclaimers / eligibility checks
) {}
