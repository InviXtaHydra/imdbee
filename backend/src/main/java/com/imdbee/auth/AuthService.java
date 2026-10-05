package com.imdbee.auth;

import com.imdbee.auth.AuthDtos.*;
import com.imdbee.common.ApiException;
import com.imdbee.security.AuthUser;
import com.imdbee.security.JwtService;
import com.imdbee.user.User;
import com.imdbee.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "Deze gebruikersnaam is al in gebruik.");
        }
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Er bestaat al een account met dit e-mailadres.");
        }
        User user = users.save(new User(
                req.username().trim(),
                req.email().trim().toLowerCase(),
                passwordEncoder.encode(req.password())));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String login = req.login().trim();
        User user = (login.contains("@") ? users.findByEmailIgnoreCase(login) : users.findByUsernameIgnoreCase(login))
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                // Same message for unknown user and wrong password, so accounts can't be probed
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Gebruikersnaam of wachtwoord klopt niet."));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return users.findById(userId).map(AuthService::toDto)
                .orElseThrow(() -> ApiException.notFound("Gebruiker niet gevonden."));
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(AuthUser.from(user));
        return new AuthResponse(token, jwtService.getExpirationSeconds(), toDto(user));
    }

    static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }
}
