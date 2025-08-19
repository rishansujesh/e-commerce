package com.shop.catalog;

import com.shop.catalog.api.ProductRequest;
import com.shop.catalog.api.ProductResponse;
import com.shop.catalog.api.ProductUpdateRequest;
import com.shop.catalog.domain.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ProductRepository repository;

    @Test
    void cacheAsideWorks() {
        ProductRequest req = new ProductRequest("sku1", "Test", BigDecimal.valueOf(10.00), 5);
        ResponseEntity<ProductResponse> create = rest.postForEntity("/v1/products", req, ProductResponse.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ProductResponse first = rest.getForObject("/v1/products/{sku}", ProductResponse.class, "sku1");
        assertThat(first.price()).isEqualByComparingTo("10.00");

        // update directly in DB; cache still old
        repository.findBySku("sku1").ifPresent(p -> {
            p.setPrice(BigDecimal.valueOf(20.00));
            repository.save(p);
        });

        ProductResponse cached = rest.getForObject("/v1/products/{sku}", ProductResponse.class, "sku1");
        assertThat(cached.price()).isEqualByComparingTo("10.00");

        ProductUpdateRequest updateReq = new ProductUpdateRequest(BigDecimal.valueOf(30.00), null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductUpdateRequest> entity = new HttpEntity<>(updateReq, headers);
        rest.exchange("/v1/products/sku1", HttpMethod.PATCH, entity, ProductResponse.class);

        ProductResponse after = rest.getForObject("/v1/products/{sku}", ProductResponse.class, "sku1");
        assertThat(after.price()).isEqualByComparingTo("30.00");
    }
}
