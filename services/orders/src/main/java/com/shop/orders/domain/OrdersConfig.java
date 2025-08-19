package com.shop.orders.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class OrdersConfig {
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
