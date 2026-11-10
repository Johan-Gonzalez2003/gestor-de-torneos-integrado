package com.torneos.common;

import java.time.Instant;

/** Formato unico de error de la API. Nunca incluye trazas ni detalles internos. */
public record ApiError(int status, String error, String message, Instant timestamp) {
}
