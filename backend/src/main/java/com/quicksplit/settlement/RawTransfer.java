package com.quicksplit.settlement;

import java.math.BigDecimal;

/**
 * Transferencia calculada por el algoritmo de simplificacion, a nivel de ids.
 * Los nombres se resuelven despues, en la capa de servicio.
 *
 * @param fromUserId usuario que paga (deudor)
 * @param toUserId   usuario que recibe (acreedor)
 * @param amount     monto a transferir
 */
public record RawTransfer(Long fromUserId, Long toUserId, BigDecimal amount) {
}
