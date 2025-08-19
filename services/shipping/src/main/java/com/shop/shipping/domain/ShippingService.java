package com.shop.shipping.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
class ShippingService {
    private final ShipmentRepository shipmentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;

    ShippingService(ShipmentRepository shipmentRepository, OutboxRepository outboxRepository, ObjectMapper mapper) {
        this.shipmentRepository = shipmentRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void process(OrderEvent event) {
        Shipment shipment = new Shipment(UUID.randomUUID(), event.orderId(), ShipmentStatus.SCHEDULED,
                "DHL", null, Instant.now());
        shipmentRepository.save(shipment);
        ShipmentEvent out = new ShipmentEvent(event.orderId(), shipment.getStatus(), shipment.getCarrier(), shipment.getTracking());
        String payload;
        try {
            payload = mapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        OutboxMessage msg = new OutboxMessage(UUID.randomUUID(), "Shipment", shipment.getId(),
                "shipment.scheduled", payload, Instant.now(), event.orderId().toString());
        outboxRepository.save(msg);
    }

    record OrderEvent(UUID orderId) {}
    record ShipmentEvent(UUID orderId, ShipmentStatus status, String carrier, String tracking) {}
}
