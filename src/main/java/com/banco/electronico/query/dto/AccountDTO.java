package com.banco.electronico.query.dto;

import com.banco.electronico.api.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private String id;               // Identificador de la cuenta
    private Instant createdAt;       // Fecha de creación
    private double balance;          // Saldo actual
    private AccountStatus status;    // Estado (CREATED, ACTIVATED, SUSPENDED)
    private String currency;         // Moneda (USD, EUR, COP)
}
