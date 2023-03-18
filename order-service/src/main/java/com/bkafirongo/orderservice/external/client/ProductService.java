package com.bkafirongo.orderservice.external.client;

import com.bkafirongo.orderservice.exception.CustomerException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@CircuitBreaker(name ="external", fallbackMethod = "fallback")
@FeignClient(name = "PRODUCT-SERVICE/products")
public interface ProductService {

    @GetMapping("/reduce-quantity/{id}")
    ResponseEntity<Void> reduceQuantity(
            @PathVariable("id") Long productId,
            @RequestParam("quantity") Long quantity);

    default void fallback(Exception e){
        throw  new CustomerException("Product service is down","UNAVAILABLE", 500);
    }
}
