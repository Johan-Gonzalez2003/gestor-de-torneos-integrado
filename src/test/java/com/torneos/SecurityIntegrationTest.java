package com.torneos;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Pruebas de punta a punta: login, JWT, roles, propiedad de recursos y cabeceras de seguridad. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String ADMIN_PASSWORD = "AdminPrueba-2026!";
    private static final String USER_PASSWORD = "UserPrueba-2026!";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ---------- Autenticacion (login) ----------

    @Test
    void loginCorrectoDevuelve200YToken() throws Exception {
        postLogin("admin", ADMIN_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        postLogin("user", "clave-equivocada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"));
    }

    @Test
    void loginConCuerpoVacioDevuelve400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void despuesDeCincoFallosElLoginDevuelve429() throws Exception {
        for (int i = 0; i < 5; i++) {
            postLogin("intruso", "mala-" + i).andExpect(status().isUnauthorized());
        }

        postLogin("intruso", "mala-final").andExpect(status().isTooManyRequests());
    }

    // ---------- Autorizacion por rol (RBAC) ----------

    @Test
    void sinTokenLaApiDevuelve401() throws Exception {
        mvc.perform(get("/api/v1/torneos")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAlteradoDevuelve401() throws Exception {
        String token = tokenOf("user", USER_PASSWORD);

        mvc.perform(get("/api/v1/user/ping").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userNoPuedeEntrarAlEndpointDeAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/ping").header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeEntrarAlEndpointDeAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/ping").header("Authorization", bearer("admin", ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("admin"));
    }

    @Test
    void userPuedeEntrarAlEndpointDeUser() throws Exception {
        mvc.perform(get("/api/v1/user/ping").header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("user"));
    }

    @Test
    void adminTambienPuedeEntrarAlEndpointDeUser() throws Exception {
        mvc.perform(get("/api/v1/user/ping").header("Authorization", bearer("admin", ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void unaRutaNoDeclaradaQuedaDenegada() throws Exception {
        mvc.perform(get("/api/v1/secreto").header("Authorization", bearer("admin", ADMIN_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    // ---------- Propiedad de recursos (BOLA) y DTOs (BOPLA) ----------

    @Test
    void crearTorneoDevuelve201SinExponerAlPropietario() throws Exception {
        mvc.perform(post("/api/v1/torneos")
                        .header("Authorization", bearer("user", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(torneoJson("Copa UMB")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$.creadoPor").doesNotExist());
    }

    @Test
    void otroUsuarioNoPuedeModificarUnTorneoAjeno() throws Exception {
        long id = createTorneo("user", USER_PASSWORD, "De user");

        mvc.perform(put("/api/v1/torneos/" + id)
                        .header("Authorization", bearer("user2", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(torneoJson("Hackeado")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeModificarUnTorneoAjeno() throws Exception {
        long id = createTorneo("user", USER_PASSWORD, "De user");

        mvc.perform(put("/api/v1/torneos/" + id)
                        .header("Authorization", bearer("admin", ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(torneoJson("Moderado")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Moderado"));
    }

    @Test
    void elPropietarioPuedeEliminarYLuegoNoExiste() throws Exception {
        long id = createTorneo("user", USER_PASSWORD, "Temporal");

        mvc.perform(delete("/api/v1/torneos/" + id).header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/torneos/" + id).header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unCampoNoPermitidoEnElCuerpoDevuelve400() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "nombre", "Copa", "videojuego", "Valorant",
                "fechaInicio", LocalDate.now().plusDays(10).toString(),
                "maxParticipantes", 16, "estado", "FINALIZADO"));

        mvc.perform(post("/api/v1/torneos")
                        .header("Authorization", bearer("user", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unCuerpoInvalidoDevuelve400ConElCampoIndicado() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "nombre", "", "videojuego", "Valorant",
                "fechaInicio", LocalDate.now().plusDays(10).toString(),
                "maxParticipantes", 1));

        mvc.perform(post("/api/v1/torneos")
                        .header("Authorization", bearer("user", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("nombre")))
                .andExpect(jsonPath("$.message").value(containsString("maxParticipantes")));
    }

    @Test
    void unIdNoNumericoDevuelve400() throws Exception {
        mvc.perform(get("/api/v1/torneos/abc").header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    // ---------- Consumo de recursos y configuracion segura ----------

    @Test
    void elListadoLimitaElTamanoDePagina() throws Exception {
        mvc.perform(get("/api/v1/torneos?size=500").header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    void lasRespuestasIncluyenCabecerasDeSeguridad() throws Exception {
        mvc.perform(get("/api/v1/user/ping").header("Authorization", bearer("user", USER_PASSWORD)))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")));
    }

    // ---------- Utilidades ----------

    private ResultActions postLogin(String username, String password) throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", username, "password", password));
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String tokenOf(String username, String password) throws Exception {
        String json = postLogin(username, password).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("token").asText();
    }

    private String bearer(String username, String password) throws Exception {
        return "Bearer " + tokenOf(username, password);
    }

    private String torneoJson(String nombre) throws Exception {
        return mapper.writeValueAsString(Map.of(
                "nombre", nombre, "videojuego", "Valorant",
                "fechaInicio", LocalDate.now().plusDays(10).toString(),
                "maxParticipantes", 16));
    }

    private long createTorneo(String username, String password, String nombre) throws Exception {
        String json = mvc.perform(post("/api/v1/torneos")
                        .header("Authorization", bearer(username, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(torneoJson(nombre)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asLong();
    }
}
