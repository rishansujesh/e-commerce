package com.shop.orders.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int qty;

    protected OrderItem() {}

    public OrderItem(UUID id, Order order, String productSku, BigDecimal unitPrice, int qty) {
        this.id = id;
        this.order = order;
        this.productSku = productSku;
        this.unitPrice = unitPrice;
        this.qty = qty;
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }
}
