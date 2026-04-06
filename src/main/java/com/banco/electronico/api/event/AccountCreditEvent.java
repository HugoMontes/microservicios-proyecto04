package com.banco.electronico.api.event;

import lombok.Getter;

public class AccountCreditEvent extends BaseEvent<String> {

    @Getter
    private String currency;    // Moneda del depósito

    @Getter
    private double amount;      // Cantidad depositada

    public AccountCreditEvent(String id, String currency, double amount) {
        super(id);  // Pasa el ID a BaseEvent
        this.currency = currency;
        this.amount = amount;
    }
}
