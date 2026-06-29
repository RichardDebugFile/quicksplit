package com.quicksplit.expense;

/**
 * Como se reparte un gasto entre los participantes.
 */
public enum SplitType {

    /** Partes iguales entre los participantes indicados (o todo el grupo). */
    EQUAL,

    /** Montos exactos por participante (deben sumar el total del gasto). */
    EXACT
}
