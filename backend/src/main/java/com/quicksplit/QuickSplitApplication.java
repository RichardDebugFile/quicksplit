package com.quicksplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion QuickSplit.
 *
 * <p>QuickSplit es una API REST para dividir gastos entre miembros de un grupo
 * y calcular el plan de pagos minimo para saldar las deudas (simplificacion de deudas).
 */
@SpringBootApplication
public class QuickSplitApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuickSplitApplication.class, args);
    }
}
