package com.tournamentmanager.api.dto;

import com.tournamentmanager.domain.Tournament;
import com.tournamentmanager.domain.TournamentStatus;

import java.time.LocalDate;

public record TournamentResponse(
        Long id,
        String name,
        String videoGame,
        LocalDate startDate,
        int maxTeams,
        TournamentStatus status,
        String description) {

    public static TournamentResponse from(Tournament tournament) {
        return new TournamentResponse(tournament.getId(), tournament.getName(), tournament.getVideoGame(),
                tournament.getStartDate(), tournament.getMaxTeams(), tournament.getStatus(),
                tournament.getDescription());
    }
}
