package com.quicksplit.settlement.dto;

import com.quicksplit.user.dto.UserDto;
import java.math.BigDecimal;

/**
 * Transferencia sugerida para saldar deudas: {@code from} le paga a {@code to} el {@code amount}.
 */
public record SettlementTransactionDto(UserDto from, UserDto to, BigDecimal amount) {
}
