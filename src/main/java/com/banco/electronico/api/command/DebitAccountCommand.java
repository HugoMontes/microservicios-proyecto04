package com.banco.electronico.api.command;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DebitAccountCommand extends BaseCommand<String> {

    @Getter
    private String currency; // Moneda del retiro (debe coincidir con la moneda de la cuenta)

    @Getter
    private double amount; // Cantidad de dinero a retirar

    public DebitAccountCommand(String id, String currency, double amount) {
        super(id);  // Pasa el ID de la cuenta a BaseCommand
        this.currency = currency;
        this.amount = amount;
    }
}