package com.bkafirongo.paymentservice.controller;

import com.bkafirongo.paymentservice.model.PaymentRequest;
import com.bkafirongo.paymentservice.model.PaymentResponse;
import com.bkafirongo.paymentservice.service.TransactionDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final TransactionDetailsService transactionDetailsService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest){
        return ResponseEntity.ok(transactionDetailsService.doPayment(paymentRequest));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable("orderId") Long id){
        return ResponseEntity.ok(transactionDetailsService.getPaymentDetailsByOrderId(id));
    }
}
