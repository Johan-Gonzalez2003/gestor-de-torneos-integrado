package com.torneos.torneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.torneos.common.ResourceNotFoundException;
import com.torneos.security.Actor;

class TorneoServiceTest {

    private static final Actor ANA = new Actor("ana", false);
    private static final Actor LUIS = new Actor("luis", false);
    private static final Actor ADMIN = new Actor("admin", true);

    private final InMemoryTorneoRepository repository = new InMemoryTorneoRepository();
    private final TorneoService service = new TorneoService(repository);

    private static TorneoRequest request(String nombre) {
        return new TorneoRequest(nombre, "Valorant", LocalDate.of(2027, 1, 15), 16);
    }

    @Test
    void crearAsignaEstadoProgramadoYPropietario() {
        TorneoResponse created = service.crear(request("Copa UMB"), ANA);

        assertThat(created.estado()).isEqualTo(EstadoTorneo.PROGRAMADO);
        assertThat(created.nombre()).isEqualTo("Copa UMB");
        assertThat(repository.findById(created.id())).get()
                .extracting(Torneo::creadoPor).isEqualTo("ana");
    }

    @Test
    void crearGeneraIdsConsecutivos() {
        TorneoResponse first = service.crear(request("Uno"), ANA);
        TorneoResponse second = service.crear(request("Dos"), ANA);

        assertThat(first.id()).isEqualTo(1L);
        assertThat(second.id()).isEqualTo(2L);
    }

    @Test
    void buscarDevuelveElTorneoExistente() {
        TorneoResponse created = service.crear(request("Copa UMB"), ANA);

        assertThat(service.buscar(created.id())).isEqualTo(created);
    }

    @Test
    void buscarInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.buscar(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listarLimitaElTamanoMaximoDePagina() {
        for (int i = 0; i < 55; i++) {
            service.crear(request("T" + i), ANA);
        }

        PageResponse<TorneoResponse> page = service.listar(0, 1000);

        assertThat(page.size()).isEqualTo(50);
        assertThat(page.content()).hasSize(50);
        assertThat(page.totalElements()).isEqualTo(55);
    }

    @Test
    void listarCorrigeValoresInvalidos() {
        service.crear(request("Uno"), ANA);
        service.crear(request("Dos"), ANA);

        PageResponse<TorneoResponse> page = service.listar(-5, 0);

        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.content()).hasSize(1);
    }

    @Test
    void listarEntregaLaPaginaSolicitada() {
        for (int i = 0; i < 5; i++) {
            service.crear(request("T" + i), ANA);
        }

        PageResponse<TorneoResponse> page = service.listar(1, 2);

        assertThat(page.content()).extracting(TorneoResponse::id).containsExactly(3L, 4L);
        assertThat(page.totalElements()).isEqualTo(5);
    }

    @Test
    void elPropietarioPuedeActualizarYConservaElEstado() {
        TorneoResponse created = service.crear(request("Viejo"), ANA);

        TorneoResponse updated = service.actualizar(created.id(), request("Nuevo"), ANA);

        assertThat(updated.nombre()).isEqualTo("Nuevo");
        assertThat(updated.estado()).isEqualTo(EstadoTorneo.PROGRAMADO);
        assertThat(updated.id()).isEqualTo(created.id());
    }

    @Test
    void otroUsuarioNoPuedeActualizarUnTorneoAjeno() {
        TorneoResponse created = service.crear(request("De Ana"), ANA);
        long id = created.id();
        TorneoRequest change = request("Hackeado");

        assertThatThrownBy(() -> service.actualizar(id, change, LUIS))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(service.buscar(id).nombre()).isEqualTo("De Ana");
    }

    @Test
    void elAdministradorPuedeActualizarCualquierTorneo() {
        TorneoResponse created = service.crear(request("De Ana"), ANA);

        TorneoResponse updated = service.actualizar(created.id(), request("Moderado"), ADMIN);

        assertThat(updated.nombre()).isEqualTo("Moderado");
    }

    @Test
    void actualizarInexistenteLanzaNotFound() {
        TorneoRequest change = request("X");

        assertThatThrownBy(() -> service.actualizar(7, change, ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void elPropietarioPuedeEliminar() {
        TorneoResponse created = service.crear(request("Temporal"), ANA);

        service.eliminar(created.id(), ANA);

        assertThat(repository.findById(created.id())).isEmpty();
    }

    @Test
    void otroUsuarioNoPuedeEliminarUnTorneoAjeno() {
        TorneoResponse created = service.crear(request("De Ana"), ANA);
        long id = created.id();

        assertThatThrownBy(() -> service.eliminar(id, LUIS)).isInstanceOf(AccessDeniedException.class);
        assertThat(repository.findById(id)).isPresent();
    }

    @Test
    void elAdministradorPuedeEliminarCualquierTorneo() {
        TorneoResponse created = service.crear(request("De Ana"), ANA);

        service.eliminar(created.id(), ADMIN);

        assertThat(repository.findById(created.id())).isEmpty();
    }
}
