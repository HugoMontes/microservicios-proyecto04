package com.banco.electronico.api.event;

import com.banco.electronico.api.enums.AccountStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AccountCreatedEvent extends BaseEvent<String> {

    @Getter
    private String currency;        // Moneda de la cuenta (USD, EUR, COP...)

    @Getter
    private double balance;         // Saldo inicial de la cuenta

    @Getter
    private AccountStatus status;   // Estado inicial (CREATED, ACTIVATED, SUSPENDED)

    public AccountCreatedEvent(String id, String currency, double balance, AccountStatus status) {
        super(id);
        this.currency = currency;
        this.balance = balance;
        this.status = status;
    }
}
