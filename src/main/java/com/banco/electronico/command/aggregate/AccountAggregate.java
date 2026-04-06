package com.banco.electronico.command.aggregate;

import com.banco.electronico.api.command.CreateAccountCommand;
import com.banco.electronico.api.command.CreditAccountCommand;
import com.banco.electronico.api.command.DebitAccountCommand;
import com.banco.electronico.api.enums.AccountStatus;
import com.banco.electronico.api.event.AccountCreatedEvent;
import com.banco.electronico.api.event.AccountCreditEvent;
import com.banco.electronico.api.event.AccountDebitEvent;
import com.banco.electronico.api.exception.NegativeInitialBalanceException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate      // Marca esta clase como un agregado gestionado por Axon
@Slf4j          // Lombok: proporciona un logger (log.info, log.error, etc.)
public class AccountAggregate {

    @AggregateIdentifier                 // Identificador único del agregado
    private String accountId;            // Identificador único de la cuenta
    private String currency;             // Moneda (USD, EUR, COP...)
    private double balance;              // Saldo actual
    private AccountStatus accountStatus; // Estado (CREATED, ACTIVATED, SUSPENDED)

    // Constructor vacio requerido por AXON
    public AccountAggregate() {
    }

    //  Maneja el comando de creación de cuenta.
    // Al estar en el constructor, se ejecuta cuando la cuenta NO existe aún.
    @CommandHandler
    public AccountAggregate(CreateAccountCommand createAccountCommand) {
        log.info("CreateAccountCommand recibido");

        // VALIDACIÓN: El saldo inicial no puede ser negativo
        if (createAccountCommand.getInitialBalance() < 0) {
            throw new NegativeInitialBalanceException("Error, no se puede tener un saldo negativo");
        }

        // Si la validación pasa, genera el evento AccountCreatedEvent
        AggregateLifecycle.apply(new AccountCreatedEvent(
                createAccountCommand.getId(),
                createAccountCommand.getCurrency(),
                createAccountCommand.getInitialBalance(),
                AccountStatus.CREATED
        ));
    }

    //  Actualiza el estado del agregado cuando ocurre un AccountCreatedEvent.
    //  Establece los valores iniciales de la cuenta.
    @EventSourcingHandler
    public void on(AccountCreatedEvent accountCreatedEvent) {
        log.info("Evento AccountCreatedEvent");
        this.accountId = accountCreatedEvent.getId();
        this.balance = accountCreatedEvent.getBalance();
        this.accountStatus = accountCreatedEvent.getStatus();
        this.currency = accountCreatedEvent.getCurrency();
    }

    // Maneja el comando de depósito (acreditar dinero).
    // Se ejecuta sobre una cuenta existente.
    @CommandHandler
    public void handle(CreditAccountCommand creditAccountCommand) {
        log.info("CreditAccountCommand recibido");

        // VALIDACIÓN: El monto a depositar no puede ser negativo
        if (creditAccountCommand.getAmount() < 0) {
            throw new NegativeInitialBalanceException("Error, no se puede tener un saldo negativo");
        }

        // Si la validación pasa, genera el evento AccountCreditEvent
        AggregateLifecycle.apply(new AccountCreditEvent(
                creditAccountCommand.getId(),
                creditAccountCommand.getCurrency(),
                creditAccountCommand.getAmount()
        ));
    }

    // Maneja el comando de retiro (debitar dinero).
    // Se ejecuta sobre una cuenta existente.
    @CommandHandler
    public void handle(DebitAccountCommand debitAccountCommand) {
        log.info("DebitAccountCommand recibido");

        // VALIDACIÓN 1: El monto a retirar no puede ser negativo
        if (debitAccountCommand.getAmount() < 0) {
            throw new NegativeInitialBalanceException("No se puede retirar montos negativos");
        }

        // VALIDACIÓN 2 (CRÍTICA): El saldo debe ser suficiente para cubrir el retiro
        if (debitAccountCommand.getAmount() > this.balance) {
            throw new RuntimeException("Saldo insuficiente");
        }

        // Si las validaciones pasan, genera el evento AccountDebitEvent
        AggregateLifecycle.apply(new AccountDebitEvent(
                debitAccountCommand.getId(),
                debitAccountCommand.getCurrency(),
                debitAccountCommand.getAmount()
        ));
    }

    // Actualiza el estado del agregado cuando ocurre un AccountCreditEvent.
    // Aumenta el saldo de la cuenta.
    @EventSourcingHandler
    public void on(AccountCreditEvent accountCreditEvent) {
        log.info("Evento AccountCreditEvent");
        this.balance += accountCreditEvent.getAmount(); // Suma al saldo
    }

    // Actualiza el estado del agregado cuando ocurre un AccountDebitEvent.
    // Disminuye el saldo de la cuenta.
    @EventSourcingHandler
    public void on(AccountDebitEvent accountDebitEvent) {
        log.info("Evento AccountDebitEvent");
        this.balance -= accountDebitEvent.getAmount(); // Resta del saldo
    }
}

