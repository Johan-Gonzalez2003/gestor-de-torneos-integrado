package com.tournamentmanager.infrastructure.persistence;

import com.tournamentmanager.application.port.out.TournamentRepositoryPort;
import com.tournamentmanager.domain.Tournament;
import com.tournamentmanager.domain.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Adaptador que implementa el puerto de repositorio con JPA y traduce entre entidad y dominio. */
@Component
public class TournamentPersistenceAdapter implements TournamentRepositoryPort {

    private final SpringDataTournamentJpaRepository jpa;

    public TournamentPersistenceAdapter(SpringDataTournamentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tournament> findAllOrderedByStartDate() {
        return jpa.findAll(Sort.by(Sort.Direction.ASC, "startDate")).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tournament> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCaseExcludingId(String name, Long id) {
        return jpa.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    @Transactional
    public Tournament save(Tournament tournament) {
        TournamentEntity entity = tournament.getId() == null
                ? new TournamentEntity()
                : jpa.findById(tournament.getId()).orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el torneo con id " + tournament.getId() + "."));
        entity.setName(tournament.getName());
        entity.setVideoGame(tournament.getVideoGame());
        entity.setStartDate(tournament.getStartDate());
        entity.setMaxTeams(tournament.getMaxTeams());
        entity.setStatus(tournament.getStatus());
        entity.setDescription(tournament.getDescription());
        return toDomain(jpa.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    private Tournament toDomain(TournamentEntity entity) {
        return Tournament.restore(entity.getId(), entity.getName(), entity.getVideoGame(),
                entity.getStartDate(), entity.getMaxTeams(), entity.getStatus(), entity.getDescription());
    }
}
