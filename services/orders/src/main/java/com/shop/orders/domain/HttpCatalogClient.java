package com.shop.orders.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
class HttpCatalogClient implements CatalogClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    HttpCatalogClient(RestTemplate restTemplate,
                      @Value("${catalog.base-url:http://catalog:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public ProductInfo getProduct(String sku) {
        Map<?, ?> resp = restTemplate.getForObject(baseUrl + "/v1/products/" + sku, Map.class);
        if (resp == null || resp.get("price") == null) {
            throw new IllegalArgumentException("sku not found");
        }
        BigDecimal price = new BigDecimal(resp.get("price").toString());
        return new ProductInfo(sku, price);
    }
}
