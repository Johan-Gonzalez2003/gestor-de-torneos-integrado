package com.tournamentmanager.domain;

import com.tournamentmanager.domain.exception.DomainValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio pura: sin JPA, sin Spring.
 * Las reglas de negocio viven aquí y se verifican al crear o modificar un torneo.
 */
public final class Tournament {

    public static final int MIN_TEAMS = 2;
    public static final int MAX_TEAMS = 1024;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_GAME_LENGTH = 80;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final Long id;
    private final String name;
    private final String videoGame;
    private final LocalDate startDate;
    private final int maxTeams;
    private final TournamentStatus status;
    private final String description;

    private Tournament(Long id, String name, String videoGame, LocalDate startDate,
                       int maxTeams, TournamentStatus status, String description) {
        this.id = id;
        this.name = name;
        this.videoGame = videoGame;
        this.startDate = startDate;
        this.maxTeams = maxTeams;
        this.status = status;
        this.description = description;
    }

    /** Crea un torneo nuevo validando todas las reglas. {@code today} se inyecta para poder probarlo. */
    public static Tournament create(String name, String videoGame, LocalDate startDate,
                                    Integer maxTeams, TournamentStatus status, String description,
                                    LocalDate today) {
        return build(null, name, videoGame, startDate, maxTeams, status, description, today);
    }

    /** Devuelve una copia del torneo con los cambios indicados, conservando el id. */
    public Tournament update(String name, String videoGame, LocalDate startDate, Integer maxTeams,
                             TournamentStatus status, String description, LocalDate today) {
        return build(this.id, name, videoGame, startDate, maxTeams,
                status == null ? this.status : status, description, today);
    }

    /** Reconstruye un torneo ya persistido. No vuelve a validar la fecha, porque pudo haber pasado. */
    public static Tournament restore(Long id, String name, String videoGame, LocalDate startDate,
                                     int maxTeams, TournamentStatus status, String description) {
        return new Tournament(id, name, videoGame, startDate, maxTeams, status, description);
    }

    private static Tournament build(Long id, String name, String videoGame, LocalDate startDate,
                                    Integer maxTeams, TournamentStatus status, String description,
                                    LocalDate today) {
        List<String> errors = new ArrayList<>();
        String cleanName = trimmed(name);
        String cleanGame = trimmed(videoGame);
        String cleanDescription = description == null ? null : description.trim();

        if (cleanName.isEmpty()) {
            errors.add("El nombre es obligatorio.");
        } else if (cleanName.length() > MAX_NAME_LENGTH) {
            errors.add("El nombre no puede superar " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (cleanGame.isEmpty()) {
            errors.add("El videojuego es obligatorio.");
        } else if (cleanGame.length() > MAX_GAME_LENGTH) {
            errors.add("El videojuego no puede superar " + MAX_GAME_LENGTH + " caracteres.");
        }
        if (startDate == null) {
            errors.add("La fecha de inicio es obligatoria.");
        } else if (today != null && startDate.isBefore(today)) {
            errors.add("La fecha de inicio no puede estar en el pasado.");
        }
        if (maxTeams == null) {
            errors.add("El cupo máximo es obligatorio.");
        } else if (maxTeams < MIN_TEAMS || maxTeams > MAX_TEAMS) {
            errors.add("El cupo debe estar entre " + MIN_TEAMS + " y " + MAX_TEAMS + " equipos.");
        }
        if (cleanDescription != null && cleanDescription.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("La descripción no puede superar " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        if (!errors.isEmpty()) {
            throw new DomainValidationException(errors);
        }
        return new Tournament(id, cleanName, cleanGame, startDate, maxTeams,
                status == null ? TournamentStatus.DRAFT : status, cleanDescription);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getVideoGame() { return videoGame; }
    public LocalDate getStartDate() { return startDate; }
    public int getMaxTeams() { return maxTeams; }
    public TournamentStatus getStatus() { return status; }
    public String getDescription() { return description; }
}
