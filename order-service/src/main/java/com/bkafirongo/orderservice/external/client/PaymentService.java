package com.bkafirongo.orderservice.external.client;

import com.bkafirongo.orderservice.exception.CustomerException;
import com.bkafirongo.orderservice.external.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CircuitBreaker(name ="external", fallbackMethod = "fallback")
@FeignClient(name = "PAYMENT-SERVICE/payments")
public interface PaymentService {

    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default ResponseEntity<Long> fallback(Exception e){
        throw  new CustomerException("Payment service is down","UNAVAILABLE", 500);
    }
}
