package com.torneos.security;

/** Roles del sistema (RBAC). Spring los usa con el prefijo ROLE_. */
public enum Role {
    ADMIN,
    USER;

    public String authority() {
        return "ROLE_" + name();
    }
}
