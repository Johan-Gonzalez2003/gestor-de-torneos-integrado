package com.torneos.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.torneos.common.AccountLockedException;
import com.torneos.common.InvalidCredentialsException;
import com.torneos.security.JwtService;
import com.torneos.security.LoginAttemptService;
import com.torneos.security.UserRepository;

/** Caso de uso "iniciar sesion": valida credenciales y entrega un JWT. */
@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       JwtService jwtService, LoginAttemptService attempts) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username();
        if (attempts.isLocked(username)) {
            throw new AccountLockedException();
        }
        // Mismo error si el usuario no existe o la clave es incorrecta (no revela cuentas validas)
        var account = users.findByUsername(username)
                .filter(u -> encoder.matches(request.password(), u.passwordHash()))
                .orElseThrow(() -> {
                    attempts.registerFailure(username);
                    return new InvalidCredentialsException();
                });
        attempts.registerSuccess(username);
        String token = jwtService.generateToken(account.username(), account.role());
        return new LoginResponse(token, "Bearer", jwtService.expirationSeconds());
    }
}
