package com.shop.orders.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotBlank String sku,
        @NotNull @Positive Integer qty
) {}
