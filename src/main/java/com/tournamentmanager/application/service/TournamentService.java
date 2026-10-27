package com.tournamentmanager.application.service;

import com.tournamentmanager.application.command.TournamentCommand;
import com.tournamentmanager.application.port.out.TournamentRepositoryPort;
import com.tournamentmanager.domain.Tournament;
import com.tournamentmanager.domain.exception.ConflictException;
import com.tournamentmanager.domain.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Casos de uso del CRUD de torneos. Las dependencias llegan por constructor. */
public class TournamentService {

    private final TournamentRepositoryPort repository;
    private final Clock clock;

    public TournamentService(TournamentRepositoryPort repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Tournament> findAll() {
        return repository.findAllOrderedByStartDate();
    }

    public Tournament findById(Long id) {
        return getOrThrow(id);
    }

    public Tournament create(TournamentCommand command) {
        Tournament candidate = Tournament.create(command.name(), command.videoGame(), command.startDate(),
                command.maxTeams(), command.status(), command.description(), today());
        if (repository.existsByNameIgnoreCase(candidate.getName())) {
            throw new ConflictException("Ya existe un torneo con ese nombre.");
        }
        return repository.save(candidate);
    }

    public Tournament update(Long id, TournamentCommand command) {
        Tournament existing = getOrThrow(id);
        Tournament updated = existing.update(command.name(), command.videoGame(), command.startDate(),
                command.maxTeams(), command.status(), command.description(), today());
        if (repository.existsByNameIgnoreCaseExcludingId(updated.getName(), id)) {
            throw new ConflictException("Ya existe otro torneo con ese nombre.");
        }
        return repository.save(updated);
    }

    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private Tournament getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el torneo con id " + id + "."));
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
