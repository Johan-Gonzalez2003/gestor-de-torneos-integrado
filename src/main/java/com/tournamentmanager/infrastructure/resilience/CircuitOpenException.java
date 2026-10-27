package com.tournamentmanager.infrastructure.resilience;

public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String circuitName) {
        super("El circuito '" + circuitName + "' está abierto: se evita llamar a la fuente.");
    }
}
