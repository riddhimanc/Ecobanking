package com.ecobank.core.domain;

public record AccountDTO(Long id, String accountNo, String type, Double balance, Long customerId) {

    public AccountDTO(Long id2, String accountNo2, String type2, Object orElse) {
        this(id2, accountNo2, type2, orElse instanceof Double ? (Double) orElse : 0.0, null);
    }

}
