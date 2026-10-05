package com.imdbee.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Gebruikersnaam is verplicht.")
            @Size(min = 3, max = 40, message = "Gebruikersnaam moet 3 tot 40 tekens lang zijn.")
            @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "Gebruik alleen letters, cijfers, punt, _ of -.")
            String username,

            @NotBlank(message = "E-mailadres is verplicht.")
            @Email(message = "Vul een geldig e-mailadres in.")
            String email,

            @NotBlank(message = "Wachtwoord is verplicht.")
            @Size(min = 8, max = 100, message = "Wachtwoord moet minstens 8 tekens lang zijn.")
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "Vul je gebruikersnaam of e-mailadres in.") String login,
            @NotBlank(message = "Vul je wachtwoord in.") String password) {
    }

    public record UserDto(Long id, String username, String email, String role, Instant createdAt) {
    }

    public record AuthResponse(String token, long expiresIn, UserDto user) {
    }
}
