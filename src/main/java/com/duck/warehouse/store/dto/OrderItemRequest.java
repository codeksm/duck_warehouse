package com.duck.warehouse.store.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotBlank String color,
        @NotBlank String size,
        @NotNull @Positive @Max(10_000_000) Integer quantity) {
}
