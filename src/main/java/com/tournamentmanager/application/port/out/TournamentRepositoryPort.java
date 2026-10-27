package com.tournamentmanager.application.port.out;

import com.tournamentmanager.domain.Tournament;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: el dominio no sabe si detrás hay JPA, JDBC o una lista en memoria. */
public interface TournamentRepositoryPort {
    List<Tournament> findAllOrderedByStartDate();
    Optional<Tournament> findById(Long id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseExcludingId(String name, Long id);
    Tournament save(Tournament tournament);
    void deleteById(Long id);
}
