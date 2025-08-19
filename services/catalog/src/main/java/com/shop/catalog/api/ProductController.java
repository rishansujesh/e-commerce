package com.shop.catalog.api;

import com.shop.catalog.domain.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products")
class ProductController {
    private final ProductService service;

    ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ProductResponse> create(@Validated @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{sku}")
    ProductResponse get(@PathVariable String sku) {
        return service.get(sku);
    }

    @PatchMapping("/{sku}")
    ProductResponse update(@PathVariable String sku, @Validated @RequestBody ProductUpdateRequest request) {
        return service.update(sku, request);
    }
}
