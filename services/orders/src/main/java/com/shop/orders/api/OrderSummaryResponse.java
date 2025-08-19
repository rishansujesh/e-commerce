package com.shop.orders.api;

import com.shop.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderSummaryResponse(UUID id, OrderStatus status, BigDecimal total) {}

