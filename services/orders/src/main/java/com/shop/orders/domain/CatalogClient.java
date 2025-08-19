package com.shop.orders.domain;

import java.math.BigDecimal;

public interface CatalogClient {
    ProductInfo getProduct(String sku);

    record ProductInfo(String sku, BigDecimal price) {}
}
