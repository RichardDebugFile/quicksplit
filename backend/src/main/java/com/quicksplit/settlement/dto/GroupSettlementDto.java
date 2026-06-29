package com.quicksplit.settlement.dto;

import java.util.List;

/**
 * Resultado completo del calculo: balances por usuario y plan de pagos minimo.
 */
public record GroupSettlementDto(
        List<BalanceDto> balances,
        List<SettlementTransactionDto> transactions) {
}
