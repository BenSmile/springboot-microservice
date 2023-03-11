package com.bkafirongo.paymentservice.service;

import com.bkafirongo.paymentservice.model.PaymentRequest;
import com.bkafirongo.paymentservice.model.PaymentResponse;

public interface TransactionDetailsService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(Long id);
}
