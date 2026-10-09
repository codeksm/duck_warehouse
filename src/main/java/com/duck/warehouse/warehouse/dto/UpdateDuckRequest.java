package com.duck.warehouse.warehouse.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Color and size are intentionally absent: they are immutable. Both fields
 * optional, at least one required.
 */
public record UpdateDuckRequest(@Positive @Digits(integer = 9, fraction = 2) Double price,
		@PositiveOrZero @Max(1_000_000) Integer quantity) {
}
