package com.shop.catalog.api;

import java.math.BigDecimal;

public record ProductResponse(
        String sku,
        String name,
        BigDecimal price,
        int stock
) {}
