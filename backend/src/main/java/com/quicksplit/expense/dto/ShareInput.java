package com.quicksplit.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Monto exacto que le corresponde a un participante (split tipo EXACT).
 */
public record ShareInput(
        @NotNull(message = "El id de usuario es obligatorio")
        Long userId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "Monto invalido")
        BigDecimal amount) {
}
