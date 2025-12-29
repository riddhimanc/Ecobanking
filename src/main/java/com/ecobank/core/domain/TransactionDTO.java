package com.ecobank.core.domain;

public record TransactionDTO(Long id, String accountNo, Double amount, String description, String createdAt) {

}
