package com.shop.orders.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
class OrderCache {
    private static final String KEY_PREFIX = "orders:summary:";
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    OrderCache(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    Optional<OrderSummary> get(UUID id) {
        String json = redis.opsForValue().get(KEY_PREFIX + id);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(json, OrderSummary.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    void put(OrderSummary summary) {
        try {
            String json = mapper.writeValueAsString(summary);
            redis.opsForValue().set(KEY_PREFIX + summary.id(), json, Duration.ofMinutes(10));
        } catch (JsonProcessingException e) {
            // ignore
        }
    }
}
