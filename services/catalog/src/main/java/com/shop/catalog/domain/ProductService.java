package com.shop.catalog.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.catalog.api.ProductRequest;
import com.shop.catalog.api.ProductResponse;
import com.shop.catalog.api.ProductUpdateRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class ProductService {
    private final ProductRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    ProductService(ProductRepository repository, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(UUID.randomUUID(), request.sku(), request.name(), request.price(), request.stock(), null);
        repository.save(product);
        redisTemplate.delete(key(request.sku()));
        return toResponse(product);
    }

    public ProductResponse get(String sku) {
        String key = key(sku);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ProductResponse.class);
            } catch (JsonProcessingException e) {
                // fall through
            }
        }
        Product product = repository.findBySku(sku).orElseThrow(() -> new EmptyResultDataAccessException(1));
        ProductResponse response = toResponse(product);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException ignored) {
        }
        return response;
    }

    @Transactional
    public ProductResponse update(String sku, ProductUpdateRequest request) {
        Product product = repository.findBySku(sku).orElseThrow(() -> new EmptyResultDataAccessException(1));
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        repository.save(product);
        redisTemplate.delete(key(sku));
        return toResponse(product);
    }

    private static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getSku(), product.getName(), product.getPrice(), product.getStock());
    }

    private static String key(String sku) {
        return "product:" + sku;
    }
}
