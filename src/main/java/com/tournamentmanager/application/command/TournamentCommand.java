package com.tournamentmanager.application.command;

import com.tournamentmanager.domain.TournamentStatus;

import java.time.LocalDate;

/** Datos de entrada de los casos de uso, independientes de HTTP. */
public record TournamentCommand(
        String name,
        String videoGame,
        LocalDate startDate,
        Integer maxTeams,
        TournamentStatus status,
        String description) {
}
