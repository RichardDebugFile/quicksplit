package com.quicksplit.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del algoritmo de simplificacion de deudas.
 */
class SettlementCalculatorTest {

    private final SettlementCalculator calculator = new SettlementCalculator();

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    @DisplayName("Sin balances no hay transferencias")
    void emptyBalancesProduceNoTransfers() {
        assertThat(calculator.simplify(Map.of())).isEmpty();
    }

    @Test
    @DisplayName("Balances en cero no generan transferencias")
    void zeroBalancesProduceNoTransfers() {
        Map<Long, BigDecimal> balances = new HashMap<>();
        balances.put(1L, bd("0.00"));
        balances.put(2L, bd("0.00"));
        assertThat(calculator.simplify(balances)).isEmpty();
    }

    @Test
    @DisplayName("Dos personas: el deudor paga al acreedor en una sola transferencia")
    void twoPeopleSingleTransfer() {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("10.00"));   // le deben 10
        balances.put(2L, bd("-10.00"));  // debe 10

        List<RawTransfer> transfers = calculator.simplify(balances);

        assertThat(transfers).hasSize(1);
        RawTransfer t = transfers.get(0);
        assertThat(t.fromUserId()).isEqualTo(2L);
        assertThat(t.toUserId()).isEqualTo(1L);
        assertThat(t.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Tres personas, pago compartido: dos transferencias hacia el pagador")
    void threePeopleEqualSplit() {
        // A pago 30 repartido entre A,B,C -> A:+20, B:-10, C:-10
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("20.00"));
        balances.put(2L, bd("-10.00"));
        balances.put(3L, bd("-10.00"));

        List<RawTransfer> transfers = calculator.simplify(balances);

        assertThat(transfers).hasSize(2);
        assertThat(transfers).allSatisfy(t -> assertThat(t.toUserId()).isEqualTo(1L));
        assertThat(totalAmount(transfers)).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Cadena de deudas se simplifica a una sola transferencia")
    void chainOfDebtsIsSimplified() {
        // A debe a B y B debe a C la misma cantidad -> B queda neutro,
        // el resultado optimo es una sola transferencia A -> C.
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("-10.00")); // A debe 10
        balances.put(2L, bd("0.00"));   // B neutro
        balances.put(3L, bd("10.00"));  // a C le deben 10

        List<RawTransfer> transfers = calculator.simplify(balances);

        assertThat(transfers).hasSize(1);
        RawTransfer t = transfers.get(0);
        assertThat(t.fromUserId()).isEqualTo(1L);
        assertThat(t.toUserId()).isEqualTo(3L);
        assertThat(t.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("Cada deudor queda saldado y se conserva el monto total")
    void everyDebtorIsSettled() {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(1L, bd("60.00"));   // acreedor grande
        balances.put(2L, bd("-25.00"));
        balances.put(3L, bd("-20.00"));
        balances.put(4L, bd("-15.00"));

        List<RawTransfer> transfers = calculator.simplify(balances);

        // El total transferido debe igualar la deuda total (60).
        assertThat(totalAmount(transfers)).isEqualByComparingTo("60.00");

        // Reaplicar las transferencias deja todos los balances en cero.
        Map<Long, BigDecimal> result = new HashMap<>(balances);
        for (RawTransfer t : transfers) {
            result.merge(t.fromUserId(), t.amount(), BigDecimal::add);   // el deudor sube
            result.merge(t.toUserId(), t.amount().negate(), BigDecimal::add); // el acreedor baja
        }
        assertThat(result.values()).allSatisfy(v -> assertThat(v).isEqualByComparingTo("0.00"));

        // No mas transferencias que (n - 1).
        assertThat(transfers.size()).isLessThanOrEqualTo(balances.size() - 1);
    }

    private static BigDecimal totalAmount(List<RawTransfer> transfers) {
        return transfers.stream().map(RawTransfer::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
