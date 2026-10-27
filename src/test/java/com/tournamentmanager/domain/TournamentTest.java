package com.tournamentmanager.domain;

import com.tournamentmanager.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TournamentTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);
    private static final LocalDate FUTURE = LocalDate.of(2030, 6, 15);

    @Test
    void createsTournamentWithTrimmedFieldsAndDefaultStatus() {
        Tournament t = Tournament.create("  Copa  ", " Valorant ", FUTURE, 8, null, null, TODAY);

        assertThat(t.getName()).isEqualTo("Copa");
        assertThat(t.getVideoGame()).isEqualTo("Valorant");
        assertThat(t.getStatus()).isEqualTo(TournamentStatus.DRAFT);
    }

    @Test
    void rejectsStartDateInThePast() {
        assertThatThrownBy(() -> Tournament.create("Copa", "Valorant", LocalDate.of(2020, 1, 1), 8, null, null, TODAY))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("pasado");
    }

    @Test
    void rejectsTeamCountOutsideRange() {
        assertThatThrownBy(() -> Tournament.create("Copa", "Valorant", FUTURE, 1, null, null, TODAY))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> Tournament.create("Copa", "Valorant", FUTURE, 1025, null, null, TODAY))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void accumulatesAllValidationErrors() {
        assertThatThrownBy(() -> Tournament.create("", "", null, null, null, null, TODAY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        ex -> assertThat(ex.getErrors()).hasSizeGreaterThanOrEqualTo(4));
    }

    @Test
    void updateKeepsIdAndAppliesChanges() {
        Tournament restored = Tournament.restore(7L, "Copa", "Valorant", FUTURE, 8, TournamentStatus.DRAFT, null);

        Tournament updated = restored.update("Liga", "CS2", FUTURE, 16, TournamentStatus.REGISTRATION_OPEN, null, TODAY);

        assertThat(updated.getId()).isEqualTo(7L);
        assertThat(updated.getName()).isEqualTo("Liga");
        assertThat(updated.getMaxTeams()).isEqualTo(16);
        assertThat(updated.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_OPEN);
    }
}
