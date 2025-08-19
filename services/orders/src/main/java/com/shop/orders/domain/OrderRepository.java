package com.shop.orders.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @EntityGraph(attributePaths = "items")
    Optional<Order> findById(UUID id);
}
