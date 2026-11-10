package com.torneos.torneo;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lo unico que el cliente puede enviar: id, estado y propietario los decide el servidor. */
public record TorneoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 50) String videojuego,
        @NotNull @FutureOrPresent LocalDate fechaInicio,
        @Min(2) @Max(256) int maxParticipantes) {
}
