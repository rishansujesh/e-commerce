package com.shop.orders.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSummary(UUID id, OrderStatus status, BigDecimal total) {}

