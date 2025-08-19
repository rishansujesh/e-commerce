package com.shop.shipping.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment")
public class Shipment {
    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    @Column(nullable = false)
    private String carrier;

    private String tracking;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Shipment() {}

    public Shipment(UUID id, UUID orderId, ShipmentStatus status, String carrier, String tracking, Instant updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.carrier = carrier;
        this.tracking = tracking;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void prePersist() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTracking() {
        return tracking;
    }

    public void setTracking(String tracking) {
        this.tracking = tracking;
    }
}
