package com.banco.electronico.api.command;

import lombok.Getter;

public class CreditAccountCommand extends BaseCommand<String> {

    @Getter
    private String currency; // Moneda del depósito (debe coincidir con la moneda de la cuenta)

    @Getter
    private double amount; // Cantidad de dinero a depositar

    public CreditAccountCommand(String id, String currency, double amount) {
        super(id); // Pasa el ID de la cuenta a BaseCommand
        this.currency = currency;
        this.amount = amount;
    }
}
