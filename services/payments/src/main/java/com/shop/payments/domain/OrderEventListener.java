package com.shop.payments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class OrderEventListener {
    private final PaymentService service;
    private final ObjectMapper mapper;

    OrderEventListener(PaymentService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "order.created")
    void listen(String payload) throws Exception {
        PaymentService.OrderEvent event = mapper.readValue(payload, PaymentService.OrderEvent.class);
        service.process(event);
    }
}
