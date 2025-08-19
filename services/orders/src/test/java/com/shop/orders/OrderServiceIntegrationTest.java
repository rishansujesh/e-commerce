package com.shop.orders;

import com.shop.orders.api.CreateOrderRequest;
import com.shop.orders.api.OrderItemRequest;
import com.shop.orders.api.OrderResponse;
import com.shop.orders.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.redis.RedisContainer;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        CatalogClient catalogClient() {
            return sku -> new CatalogClient.ProductInfo(sku, BigDecimal.TEN);
        }
    }

    @Autowired
    OrderService service;

    @Autowired
    OutboxPublisher publisher;

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void createsOrderAndPublishesOutbox() {
        CreateOrderRequest req = new CreateOrderRequest(List.of(new OrderItemRequest("sku1", 1)));
        OrderResponse resp = service.createOrder(req);
        assertThat(outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()).hasSize(1);
        publisher.publish();
        assertThat(outboxRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()).isEmpty();
        String key = "orders:summary:" + resp.id();
        assertThat(redisTemplate.hasKey(key)).isTrue();
        redisTemplate.delete(key);
        assertThat(redisTemplate.hasKey(key)).isFalse();
        service.getOrder(resp.id());
        assertThat(redisTemplate.hasKey(key)).isTrue();
    }
}
