package com.quicksplit.expense.dto;

import com.quicksplit.expense.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Datos para registrar un gasto en un grupo.
 *
 * <p>Para {@code splitType = EQUAL} se usan los {@code participantUserIds} (o todo el grupo
 * si la lista viene vacia). Para {@code splitType = EXACT} se usan los {@code shares}.
 */
public record CreateExpenseRequest(
        @NotBlank(message = "La descripcion es obligatoria")
        @Size(min = 1, max = 140, message = "La descripcion debe tener entre 1 y 140 caracteres")
        String description,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "Monto invalido")
        BigDecimal amount,

        @NotNull(message = "Debe indicarse quien pago")
        Long paidByUserId,

        @NotNull(message = "El tipo de division es obligatorio")
        SplitType splitType,

        List<Long> participantUserIds,

        @Valid
        List<ShareInput> shares) {
}
