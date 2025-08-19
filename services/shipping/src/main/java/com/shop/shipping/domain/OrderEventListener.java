package com.shop.shipping.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class OrderEventListener {
    private final ShippingService service;
    private final ObjectMapper mapper;

    OrderEventListener(ShippingService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "order.confirmed")
    void listen(String payload) throws Exception {
        ShippingService.OrderEvent event = mapper.readValue(payload, ShippingService.OrderEvent.class);
        service.process(event);
    }
}
