package com.shop.shipping.domain;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class OutboxPublisher {
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    OutboxPublisher(OutboxRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "PT5S")
    void publish() {
        repository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc().forEach(msg -> {
            kafkaTemplate.send(msg.getEventType(), msg.getEventKey(), msg.getPayload());
            msg.markPublished();
            repository.save(msg);
        });
    }
}
