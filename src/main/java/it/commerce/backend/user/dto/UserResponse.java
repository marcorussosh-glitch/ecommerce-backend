package it.commerce.backend.user.dto;

import it.commerce.backend.user.UserStatus;
import it.commerce.backend.user.UserType;

import java.time.LocalDateTime;

public record UserResponse (
        Long id,
        String email,
        String firstName,
        String lastName,
        UserType userType,
        UserStatus userStatus,
        Boolean emailVerified,
        LocalDateTime createdAt
) {
}
