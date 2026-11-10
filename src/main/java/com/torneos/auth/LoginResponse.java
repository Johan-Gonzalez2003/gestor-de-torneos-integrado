package com.torneos.auth;

public record LoginResponse(String token, String tokenType, long expiresInSeconds) {
}
