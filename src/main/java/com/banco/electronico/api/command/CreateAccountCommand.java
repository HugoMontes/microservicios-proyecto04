package com.banco.electronico.api.command;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateAccountCommand extends BaseCommand<String> {

    @Getter
    private String currency;  // Moneda (USD, BS, EUR,...)

    @Getter
    private double initialBalance; // Saldo inicial de la cuenta

    public CreateAccountCommand(String id, String currency, double initialBalance) {
        super(id); // Pasa el ID a BaseCommand
        this.currency = currency;
        this.initialBalance = initialBalance;
    }
}
