package com.duck.warehouse.store.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderRequest(
        @NotEmpty @Size(max = 50) List<@NotNull @Valid OrderItemRequest> items,
        @NotBlank String country,
        @NotBlank String shippingMode) {
}
