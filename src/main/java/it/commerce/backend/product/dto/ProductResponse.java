package it.commerce.backend.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse (
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName
) {
}
