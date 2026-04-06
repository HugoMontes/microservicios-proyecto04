package com.banco.electronico.query.dto;

import com.banco.electronico.api.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountWatchEvent {
    private Instant instant;            // Momento exacto en que ocurrió la transacción (Fecha/Hora)
    private String accountId;           // Identificador de la cuenta afectada
    private double currentBalance;      // Saldo actual de la cuenta DESPUÉS de la transacción
    private TransactionType type;       // Tipo de transacción: CREDIT (depósito) o DEBIT (retiro)
    private double transactionAmount;   // Monto de la transacción (positivo siempre)
}
