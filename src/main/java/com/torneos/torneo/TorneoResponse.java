package com.torneos.torneo;

import java.time.LocalDate;

/** Vista publica de un torneo: no incluye el propietario (OWASP API3:2023). */
public record TorneoResponse(
        Long id,
        String nombre,
        String videojuego,
        LocalDate fechaInicio,
        int maxParticipantes,
        EstadoTorneo estado) {

    public static TorneoResponse from(Torneo t) {
        return new TorneoResponse(t.id(), t.nombre(), t.videojuego(), t.fechaInicio(),
                t.maxParticipantes(), t.estado());
    }
}
