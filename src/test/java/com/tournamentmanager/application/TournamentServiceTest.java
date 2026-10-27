package com.tournamentmanager.application;

import com.tournamentmanager.application.command.TournamentCommand;
import com.tournamentmanager.application.port.out.TournamentRepositoryPort;
import com.tournamentmanager.application.service.TournamentService;
import com.tournamentmanager.domain.Tournament;
import com.tournamentmanager.domain.exception.ConflictException;
import com.tournamentmanager.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba de testabilidad: el servicio se prueba con un repositorio simulado (Mockito), sin base de datos. */
class TournamentServiceTest {

    private static final LocalDate FUTURE = LocalDate.of(2030, 6, 15);

    @Mock
    private TournamentRepositoryPort repository;

    private TournamentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Clock clock = Clock.fixed(Instant.parse("2026-10-27T00:00:00Z"), ZoneOffset.UTC);
        service = new TournamentService(repository, clock);
    }

    @Test
    void createSavesTournamentWhenNameIsFree() {
        when(repository.existsByNameIgnoreCase("Copa")).thenReturn(false);
        when(repository.save(any(Tournament.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(new TournamentCommand("Copa", "Valorant", FUTURE, 8, null, null));

        ArgumentCaptor<Tournament> captor = ArgumentCaptor.forClass(Tournament.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Copa");
    }

    @Test
    void createRejectsDuplicateNameWithoutSaving() {
        when(repository.existsByNameIgnoreCase("COPA")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new TournamentCommand("COPA", "Valorant", FUTURE, 8, null, null)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteChecksExistenceBeforeRemoving() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
