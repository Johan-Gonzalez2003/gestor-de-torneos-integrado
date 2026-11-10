package com.torneos.torneo;

import java.time.LocalDate;

/**
 * Entidad de dominio (nunca se expone directamente por la API).
 * "creadoPor" sirve para el control de propiedad (OWASP API1:2023).
 */
public record Torneo(
        Long id,
        String nombre,
        String videojuego,
        LocalDate fechaInicio,
        int maxParticipantes,
        EstadoTorneo estado,
        String creadoPor) {

    /** Devuelve una copia con los datos editables cambiados; id, estado y dueno se conservan. */
    public Torneo conDatos(String nombre, String videojuego, LocalDate fechaInicio, int maxParticipantes) {
        return new Torneo(id, nombre, videojuego, fechaInicio, maxParticipantes, estado, creadoPor);
    }
}
