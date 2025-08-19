package com.shop.shipping.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
public class OutboxMessage {
    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "event_key", nullable = false, unique = true)
    private String eventKey;

    protected OutboxMessage() {}

    public OutboxMessage(UUID id, String aggregateType, UUID aggregateId, String eventType,
                         String payload, Instant createdAt, String eventKey) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
        this.eventKey = eventKey;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getEventKey() { return eventKey; }
    public void markPublished() { this.publishedAt = Instant.now(); }
}
