package com.quicksplit.settlement.dto;

import com.quicksplit.user.dto.UserDto;
import java.math.BigDecimal;

/**
 * Balance neto de un usuario en un grupo.
 * Positivo: le deben dinero. Negativo: debe dinero.
 */
public record BalanceDto(UserDto user, BigDecimal balance) {
}
