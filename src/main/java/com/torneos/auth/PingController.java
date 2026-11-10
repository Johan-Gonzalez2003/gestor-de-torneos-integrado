package com.torneos.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de prueba pedidos por la guia: uno solo para ADMIN y otro para USER (y ADMIN). */
@RestController
@RequestMapping("/api/v1")
public class PingController {

    public record PingResponse(String message, String user) {
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public PingResponse adminPing(Authentication authentication) {
        return new PingResponse("Acceso de administrador", authentication.getName());
    }

    @GetMapping("/user/ping")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PingResponse userPing(Authentication authentication) {
        return new PingResponse("Acceso de usuario", authentication.getName());
    }
}
