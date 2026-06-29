package com.quicksplit.expense.dto;

import com.quicksplit.user.dto.UserDto;
import java.math.BigDecimal;

/**
 * Parte de un gasto que corresponde a un usuario, ya resuelta para la respuesta.
 */
public record ShareDto(UserDto user, BigDecimal amount) {
}
