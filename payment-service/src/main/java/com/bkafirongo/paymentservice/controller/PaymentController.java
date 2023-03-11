package com.bkafirongo.paymentservice.controller;

import com.bkafirongo.paymentservice.model.PaymentRequest;
import com.bkafirongo.paymentservice.model.PaymentResponse;
import com.bkafirongo.paymentservice.service.TransactionDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Log4j2
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
