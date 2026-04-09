package com.banco.electronico.api.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AccountDebitEvent extends BaseEvent<String> {

    @Getter
    private String currency;    // Moneda del retiro

    @Getter
    private double amount;      // Cantidad retirada

    public AccountDebitEvent(String id, String currency, double amount) {
        super(id);              // Pasa el ID a BaseEvent
        this.currency = currency;
        this.amount = amount;
    }
}
