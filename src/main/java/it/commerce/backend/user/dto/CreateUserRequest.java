package it.commerce.backend.user.dto;

import it.commerce.backend.user.UserType;
import jakarta.validation.constraints.*;

public record CreateUserRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull
        UserType userType

) {
}

