package it.commerce.backend.user.dto;

import it.commerce.backend.user.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserStatusRequest(
        @NotNull
        UserStatus userStatus
) {
}

