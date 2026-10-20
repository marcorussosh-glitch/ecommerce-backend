package it.commerce.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest (

        @NotBlank
        @Size(max =150)
        String name,

        String description,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stockQuantity,

        @NotNull
        @Positive
        Long categoryId
) {
}
