package com.banco.electronico.query.controller;

import com.banco.electronico.query.dto.AccountDTO;
import com.banco.electronico.query.dto.AccountListDTO;
import com.banco.electronico.query.query.GetAccountById;
import com.banco.electronico.query.query.GetAllAccounts;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/query/account")
public class AccountQueryController {

    // Gateway de Axon para enviar queries (consultas) al Query Bus
    @Autowired
    private QueryGateway queryGateway;

    @GetMapping("/list")
    public CompletableFuture<AccountListDTO> listarCuentas() {
        // 1. Crear la query (sin parámetros porque pide todas)
        GetAllAccounts query = new GetAllAccounts();
        // 2. Enviar la query a Axon y especificar que esperamos una lista de Account
        return queryGateway.query(
                query,                                         // La query a enviar
                ResponseTypes.instanceOf(AccountListDTO.class) // Tipo de respuesta esperado
        );
    }

    @GetMapping("/byId/{id}")
    public CompletableFuture<AccountDTO> listarCuentaPorId(@PathVariable String id) {
        // 1. Crear la query con el ID recibido como parámetro
        GetAccountById query = new GetAccountById(id);
        // 2. Enviar la query a Axon y especificar que esperamos una sola instancia de Account
        return queryGateway.query(
                query,                                      // La query a enviar
                ResponseTypes.instanceOf(AccountDTO.class)     // Tipo de respuesta esperado
        );
    }
}
