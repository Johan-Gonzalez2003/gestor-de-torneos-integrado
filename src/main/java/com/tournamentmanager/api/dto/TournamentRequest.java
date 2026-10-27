package com.tournamentmanager.api.dto;

import com.tournamentmanager.application.command.TournamentCommand;
import com.tournamentmanager.domain.TournamentStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record TournamentRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        String name,

        @NotBlank(message = "El videojuego es obligatorio.")
        @Size(max = 80, message = "El videojuego no puede superar 80 caracteres.")
        String videoGame,

        @NotNull(message = "La fecha de inicio es obligatoria.")
        @FutureOrPresent(message = "La fecha de inicio no puede estar en el pasado.")
        LocalDate startDate,

        @NotNull(message = "El cupo máximo es obligatorio.")
        @Min(value = 2, message = "El torneo debe admitir al menos 2 equipos.")
        @Max(value = 1024, message = "El cupo máximo permitido es 1024 equipos.")
        Integer maxTeams,

        TournamentStatus status,

        @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres.")
        String description
) {
    public TournamentCommand toCommand() {
        return new TournamentCommand(name, videoGame, startDate, maxTeams, status, description);
    }
}
