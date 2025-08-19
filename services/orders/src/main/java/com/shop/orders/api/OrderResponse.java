package com.shop.orders.api;

import com.shop.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, OrderStatus status, BigDecimal total, List<Item> items) {
    public record Item(String sku, BigDecimal price, int qty) {}
}
