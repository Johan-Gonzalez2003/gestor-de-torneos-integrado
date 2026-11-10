package com.torneos.security;

import org.springframework.security.core.Authentication;

/** Quien hace la peticion: nombre de usuario y si es administrador. */
public record Actor(String username, boolean admin) {

    public static Actor from(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> Role.ADMIN.authority().equals(a.getAuthority()));
        return new Actor(authentication.getName(), admin);
    }
}
