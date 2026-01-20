package com.ecobank.core.domain;

import java.util.List;

import com.ecobank.core.models.Account;

public record AccountDetailsApiResponse(
    Account account,
    List<TransactionDTO> transactions,
    List<OpportunityDTO> opportunities
) {}
