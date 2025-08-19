package com.shop.catalog.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Positive BigDecimal price,
        @PositiveOrZero Integer stock
) {}
