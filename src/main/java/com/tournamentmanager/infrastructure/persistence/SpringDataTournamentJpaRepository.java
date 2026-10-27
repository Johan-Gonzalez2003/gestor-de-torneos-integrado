package com.tournamentmanager.infrastructure.persistence;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTournamentJpaRepository extends JpaRepository<TournamentEntity, Long> {
    List<TournamentEntity> findAll(Sort sort);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
