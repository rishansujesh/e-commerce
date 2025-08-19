package com.shop.payments.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper mapper;

    PaymentService(PaymentRepository paymentRepository, OutboxRepository outboxRepository, ObjectMapper mapper) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.mapper = mapper;
    }

    @Transactional
    public void process(OrderEvent event) {
        PaymentStatus status = event.total().compareTo(new BigDecimal("1000")) < 0 ?
                PaymentStatus.AUTHORIZED : PaymentStatus.FAILED;
        Payment payment = new Payment(UUID.randomUUID(), event.orderId(), status, event.total(), Instant.now());
        paymentRepository.save(payment);
        PaymentEvent out = new PaymentEvent(event.orderId(), status, event.total());
        String payload;
        try {
            payload = mapper.writeValueAsString(out);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String type = status == PaymentStatus.AUTHORIZED ? "payment.authorized" : "payment.failed";
        OutboxMessage msg = new OutboxMessage(UUID.randomUUID(), "Payment", payment.getId(), type,
                payload, Instant.now(), event.orderId().toString());
        outboxRepository.save(msg);
    }

    record OrderEvent(UUID orderId, BigDecimal total) {}
    record PaymentEvent(UUID orderId, PaymentStatus status, BigDecimal amount) {}
}
