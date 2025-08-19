package com.shop.orders.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.orders.api.CreateOrderRequest;
import com.shop.orders.api.OrderResponse;
import com.shop.orders.api.OrderItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final CatalogClient catalogClient;
    private final ObjectMapper objectMapper;
    private final OrderCache cache;

    OrderService(OrderRepository orderRepository, OutboxRepository outboxRepository,
                 CatalogClient catalogClient, ObjectMapper objectMapper, OrderCache cache) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.catalogClient = catalogClient;
        this.objectMapper = objectMapper;
        this.cache = cache;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(UUID.randomUUID(), OrderStatus.PENDING, BigDecimal.ZERO, null, null);
        for (OrderItemRequest itemReq : request.items()) {
            CatalogClient.ProductInfo prod = catalogClient.getProduct(itemReq.sku());
            OrderItem item = new OrderItem(UUID.randomUUID(), order, prod.sku(), prod.price(), itemReq.qty());
            order.getItems().add(item);
            order.setTotal(order.getTotal().add(item.subtotal()));
        }
        orderRepository.save(order);
        // outbox
        OrderEvent event = new OrderEvent(order.getId(), order.getStatus(), order.getTotal(),
                order.getItems().stream().map(i -> new OrderEventItem(i.getId(), i.getProductSku(), i.getUnitPrice(), i.getQty())).collect(Collectors.toList()));
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        OutboxMessage msg = new OutboxMessage(UUID.randomUUID(), "Order", order.getId(), "order.created", payload, Instant.now(), order.getId().toString());
        outboxRepository.save(msg);
        cache.put(new OrderSummary(order.getId(), order.getStatus(), order.getTotal()));
        return toResponse(order);
    }

    @Transactional
    public void handlePayment(String topic, OrderPaymentEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElseThrow();
        if (topic.equals("payment.authorized")) {
            order.setStatus(OrderStatus.CONFIRMED);
            createOutbox(order, "order.confirmed");
        } else if (topic.equals("payment.failed")) {
            order.setStatus(OrderStatus.CANCELLED);
            createOutbox(order, "order.cancelled");
        }
        cache.put(new OrderSummary(order.getId(), order.getStatus(), order.getTotal()));
    }

    private void createOutbox(Order order, String eventType) {
        OrderEvent event = new OrderEvent(order.getId(), order.getStatus(), order.getTotal(), List.of());
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessage msg = new OutboxMessage(UUID.randomUUID(), "Order", order.getId(), eventType, payload, Instant.now(), order.getId().toString()+"-"+eventType);
            outboxRepository.save(msg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public OrderSummary getOrder(UUID id) {
        return cache.get(id).orElseGet(() -> {
            Order order = orderRepository.findById(id).orElseThrow();
            OrderSummary summary = new OrderSummary(order.getId(), order.getStatus(), order.getTotal());
            cache.put(summary);
            return summary;
        });
    }



    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(OrderStatus.CANCELLED);
        createOutbox(order, "order.cancelled");
        cache.put(new OrderSummary(order.getId(), order.getStatus(), order.getTotal()));
        return toResponse(order);
    }

    private static OrderResponse toResponse(Order order) {
        List<OrderResponse.Item> items = order.getItems().stream()
                .map(i -> new OrderResponse.Item(i.getProductSku(), i.getUnitPrice(), i.getQty()))
                .collect(Collectors.toList());
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(), items);
    }

    record OrderEvent(UUID orderId, OrderStatus status, BigDecimal total, List<OrderEventItem> items) {}
    record OrderEventItem(UUID id, String sku, BigDecimal price, int qty) {}
    public record OrderPaymentEvent(UUID orderId) {}
}
