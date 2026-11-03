package it.commerce.backend.auth.dto;

public record LoginResponse (
        String message,
        String email
) {}
