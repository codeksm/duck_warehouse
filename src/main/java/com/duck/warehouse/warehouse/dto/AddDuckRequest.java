package com.duck.warehouse.warehouse.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddDuckRequest(
        @NotBlank String color,
        @NotBlank String size,
        @NotNull @Positive @Digits(integer = 9, fraction = 2) Double price,
        @NotNull @Positive @Max(1_000_000) Integer quantity) {
}
