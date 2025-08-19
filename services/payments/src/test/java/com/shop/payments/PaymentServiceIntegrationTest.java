package com.shop.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.payments.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PaymentServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    OrderEventListener listener;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    OutboxRepository outboxRepository;
    @Autowired
    OutboxPublisher publisher;
    @Autowired
    ObjectMapper mapper;

    @Test
    void processesOrderCreated() throws Exception {
        PaymentService.OrderEvent event = new PaymentService.OrderEvent(UUID.randomUUID(), BigDecimal.valueOf(100));
        listener.listen(mapper.writeValueAsString(event));
        assertThat(paymentRepository.findAll()).hasSize(1);
        assertThat(outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()).hasSize(1);
        publisher.publish();
        assertThat(outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()).isEmpty();
    }
}
