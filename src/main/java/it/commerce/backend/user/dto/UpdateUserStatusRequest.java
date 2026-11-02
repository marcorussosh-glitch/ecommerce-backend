package it.commerce.backend.user.dto;

import it.commerce.backend.user.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull
        UserStatus userStatus
) {
}

