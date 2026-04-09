package com.banco.electronico.query.service;

import com.banco.electronico.api.enums.TransactionType;
import com.banco.electronico.api.event.AccountCreatedEvent;
import com.banco.electronico.api.event.AccountCreditEvent;
import com.banco.electronico.api.event.AccountDebitEvent;
import com.banco.electronico.query.dto.AccountDTO;
import com.banco.electronico.query.dto.AccountListDTO;
import com.banco.electronico.query.dto.AccountWatchEvent;
import com.banco.electronico.query.entity.Account;
import com.banco.electronico.query.entity.AccountTransaction;
import com.banco.electronico.query.query.GetAccountBalanceStream;
import com.banco.electronico.query.query.GetAccountById;
import com.banco.electronico.query.query.GetAllAccounts;
import com.banco.electronico.query.repository.AccountRepository;
import com.banco.electronico.query.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class AccountEventHandlerService {

    @Autowired
    private AccountRepository accountRepository;          // Para operaciones con cuentas

    @Autowired
    private TransactionRepository transactionRepository;  // Para operaciones con transacciones

    @Autowired
    private QueryUpdateEmitter queryUpdateEmitter;        // Emisor de actualizaciones en tiempo real

    @EventHandler
    public void on(AccountCreatedEvent accountCreatedEvent, EventMessage<AccountCreatedEvent> eventEventMessage) {
        log.info("*********************************");
        log.info("AccountCreatedEvent recibido");
        Account account = new Account();
        account.setId(accountCreatedEvent.getId());             // Mismo ID que el agregado
        account.setBalance(accountCreatedEvent.getBalance());   // Saldo inicial
        account.setStatus(accountCreatedEvent.getStatus());     // Estado CREATED
        account.setCurrency(accountCreatedEvent.getCurrency()); // Moneda
        account.setCreatedAt(eventEventMessage.getTimestamp()); // Fecha de creación (del evento)
        // Guardar en la base de datos de lectura
        accountRepository.save(account);
    }

    // Maneja el evento de depósito (crédito).
    @EventHandler
    public void on(AccountCreditEvent accountCreditEvent, EventMessage<AccountCreatedEvent> eventMessage) {
        log.info("*********************************");
        log.info("AccountCreditEvent recibido");
        // 1. Buscar la cuenta en la base de datos de lectura
        Account account = accountRepository.findById(accountCreditEvent.getId()).get();
        // 2. Crear el registro de transacción (historial)
        AccountTransaction accountTransaction = AccountTransaction.builder()
                .account(account)                           // Cuenta asociada
                .amount(accountCreditEvent.getAmount())     // Monto depositado
                .transactionType(TransactionType.CREDIT)    // Tipo: depósito
                .timestamp(eventMessage.getTimestamp())     // Fecha/hora del evento
                .build();
        transactionRepository.save(accountTransaction);
        // 3. Actualizar el saldo en la tabla Account (sumar)
        account.setBalance(account.getBalance() + accountCreditEvent.getAmount());
        accountRepository.save(account);
        // 4. Emitir evento en tiempo real para clientes conectados (WebSockets/SSE)
        AccountWatchEvent accountWatchEvent = new AccountWatchEvent(
                accountTransaction.getTimestamp(),
                account.getId(),
                account.getBalance(),
                accountTransaction.getTransactionType(),
                accountTransaction.getAmount()
        );
        // Enviar solo a los clientes que están suscritos a esta cuenta específica
        queryUpdateEmitter.emit(GetAccountBalanceStream.class, (query)
                -> (query.getAccountId().equals(account.getId())), accountWatchEvent);
    }

    // Maneja el evento de retiro (débito).
    @EventHandler
    public void on(AccountDebitEvent accountDebitEvent, EventMessage<AccountCreatedEvent> eventMessage) {
        log.info("*********************************");
        log.info("AccountDebitEvent recibido");
        // 1. Buscar la cuenta en la base de datos de lectura
        Account account = accountRepository.findById(accountDebitEvent.getId()).get();
        // 2. Crear el registro de transacción (historial)
        AccountTransaction accountTransaction = AccountTransaction.builder()
                .account(account)                         // Cuenta asociada
                .amount(accountDebitEvent.getAmount())    // Monto retirado
                .transactionType(TransactionType.DEBIT)   // Tipo: retiro
                .timestamp(eventMessage.getTimestamp())   // Fecha/hora del evento
                .build();
        transactionRepository.save(accountTransaction);
        // 3. Actualizar el saldo en la tabla Account (restar)
        account.setBalance(account.getBalance() - accountDebitEvent.getAmount());
        accountRepository.save(account);
        // 4. Emitir evento en tiempo real para clientes conectados
        AccountWatchEvent accountWatchEvent = new AccountWatchEvent(
                accountTransaction.getTimestamp(),
                account.getId(),
                account.getBalance(),
                accountTransaction.getTransactionType(),
                accountTransaction.getAmount()
        );
        queryUpdateEmitter.emit(GetAccountBalanceStream.class,
                (query) -> (query.getAccountId().equals(account.getId())),
                accountWatchEvent);
    }

    // Maneja la query para obtener todas las cuentas.
    @QueryHandler
    public AccountListDTO on(GetAllAccounts query) {
        //  SELECT * FROM account
        List<Account> accounts = accountRepository.findAll();
        // Transformar la lista de Entity a DTOs
        List<AccountDTO> result = accounts.stream()
                .map(account -> AccountDTO.builder()
                        .id(account.getId())
                        .createdAt(account.getCreatedAt())
                        .balance(account.getBalance())
                        .status(account.getStatus())
                        .currency(account.getCurrency())
                        .build())
                .toList();
        // Retornar la lista envuelta en un wrapper AccountListDTO
        // para que Axon serialice correctamente
        return new AccountListDTO(result);
    }

    // Maneja la query para obtener una cuenta por su ID.
    @QueryHandler
    public AccountDTO on(GetAccountById query) {
        // SELECT * FROM account WHERE id = ?
        Account account = accountRepository.findById(query.getAccountId()).get();
        return AccountDTO.builder()
                .id(account.getId())
                .createdAt(account.getCreatedAt())
                .balance(account.getBalance())
                .status(account.getStatus())
                .currency(account.getCurrency())
                .build();
    }

    // Maneja la query de streaming para monitorear cambios en tiempo real.
    @QueryHandler
    public AccountWatchEvent on(GetAccountBalanceStream query) {
        // Buscar la cuenta
        Account account = accountRepository.findById(query.getAccountId()).get();
        // Buscar la última transacción de esta cuenta (para mostrar el movimiento más reciente)
        AccountTransaction accountTransaction = transactionRepository
                .findTop1ByAccountIdOrderByTimestampDesc(query.getAccountId());

        if (accountTransaction != null) {
            // Devolver estado inicial (saldo actual + última transacción)
            return new AccountWatchEvent(
                    accountTransaction.getTimestamp(),
                    account.getId(),
                    account.getBalance(),
                    accountTransaction.getTransactionType(),
                    accountTransaction.getAmount()
            );
        }
        return null; // No hay transacciones aún
    }
}
