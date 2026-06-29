package com.quicksplit.settlement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.springframework.stereotype.Component;

/**
 * Algoritmo de simplificacion de deudas (problema de "minimum cash flow").
 *
 * <p>Dado el balance neto de cada miembro de un grupo (lo que pago menos lo que le
 * corresponde pagar), produce un conjunto reducido de transferencias para saldar todas
 * las cuentas. Se usa una estrategia voraz que en cada paso empareja al mayor acreedor
 * con el mayor deudor; esto minimiza en la practica el numero de pagos necesarios.
 *
 * <p>La clase es pura (sin dependencias de Spring Data) para poder probarla de forma aislada.
 */
@Component
public class SettlementCalculator {

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    /** Nodo mutable usado en las colas de prioridad. */
    private record Party(Long userId, BigDecimal amount) { }

    /**
     * Calcula las transferencias minimas para saldar el grupo.
     *
     * @param balances mapa userId -&gt; balance neto (positivo = le deben, negativo = debe).
     *                 La suma de todos los balances debe ser cero.
     * @return lista de transferencias (de deudor a acreedor) ordenadas por monto descendente.
     */
    public List<RawTransfer> simplify(Map<Long, BigDecimal> balances) {
        // Acreedores: balance positivo. Deudores: balance negativo (se guarda en positivo).
        PriorityQueue<Party> creditors = new PriorityQueue<>(maxByAmount());
        PriorityQueue<Party> debtors = new PriorityQueue<>(maxByAmount());

        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            BigDecimal net = scale(entry.getValue());
            if (net.compareTo(EPSILON) >= 0) {
                creditors.add(new Party(entry.getKey(), net));
            } else if (net.compareTo(EPSILON.negate()) <= 0) {
                debtors.add(new Party(entry.getKey(), net.negate()));
            }
            // Balances practicamente en cero se ignoran.
        }

        List<RawTransfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Party creditor = creditors.poll();
            Party debtor = debtors.poll();

            BigDecimal settled = creditor.amount().min(debtor.amount());
            transfers.add(new RawTransfer(debtor.userId(), creditor.userId(), settled));

            BigDecimal creditorRemainder = creditor.amount().subtract(settled);
            BigDecimal debtorRemainder = debtor.amount().subtract(settled);

            if (creditorRemainder.compareTo(EPSILON) >= 0) {
                creditors.add(new Party(creditor.userId(), creditorRemainder));
            }
            if (debtorRemainder.compareTo(EPSILON) >= 0) {
                debtors.add(new Party(debtor.userId(), debtorRemainder));
            }
        }
        return transfers;
    }

    private static Comparator<Party> maxByAmount() {
        return Comparator.comparing(Party::amount).reversed();
    }

    private static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
