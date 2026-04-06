package com.banco.electronico.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDTO {
    private String currency;         // Tipo de moneda
    private double initialBalance;   // Monto inicial en la cuenta
}
