package com.shop.orders.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;

@Component
class PaymentEventListener {
    private final OrderService service;
    private final ObjectMapper mapper;

    PaymentEventListener(OrderService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @KafkaListener(topics = {"payment.authorized", "payment.failed"})
    void listen(String payload, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws Exception {
        OrderService.OrderPaymentEvent event = mapper.readValue(payload, OrderService.OrderPaymentEvent.class);
        service.handlePayment(topic, event);
    }
}
