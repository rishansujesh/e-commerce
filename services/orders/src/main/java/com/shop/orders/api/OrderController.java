package com.shop.orders.api;

import com.shop.orders.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
class OrderController {
    private final OrderService service;

    OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Validated @RequestBody CreateOrderRequest request) {
        OrderResponse resp = service.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    OrderSummaryResponse get(@PathVariable UUID id) {
        return toResponse(service.getOrder(id));
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable UUID id) {
        return service.cancelOrder(id);
    }

    private static OrderSummaryResponse toResponse(com.shop.orders.domain.OrderSummary summary) {
        return new OrderSummaryResponse(summary.id(), summary.status(), summary.total());
    }
}
