package com.tournamentmanager.api;

import com.tournamentmanager.api.dto.TournamentRequest;
import com.tournamentmanager.domain.TournamentStatus;
import com.tournamentmanager.infrastructure.persistence.SpringDataTournamentJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TournamentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataTournamentJpaRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void createsAndFetchesTournament() throws Exception {
        TournamentRequest request = new TournamentRequest("Copa Uni", "Valorant",
                LocalDate.of(2030, 6, 15), 8, TournamentStatus.REGISTRATION_OPEN, "Eliminación directa");

        String location = mockMvc.perform(post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Copa Uni"));
    }

    @Test
    void rejectsInvalidPayloadWithBadRequest() throws Exception {
        String invalid = "{\"name\":\"\",\"videoGame\":\"\",\"maxTeams\":1}";

        mockMvc.perform(post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void returnsNotFoundForMissingTournament() throws Exception {
        mockMvc.perform(get("/api/tournaments/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("No se encontró")));
    }

    @Test
    void openDataSummaryReportsUnavailableSourcesWithoutFailing() throws Exception {
        mockMvc.perform(get("/api/opendata/summary?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$[1].status").value("UNAVAILABLE"));
    }
}
