package com.bkafirongo.paymentservice.service;

import com.bkafirongo.paymentservice.entity.TransactionDetails;
import com.bkafirongo.paymentservice.exception.CustomerException;
import com.bkafirongo.paymentservice.model.PaymentMode;
import com.bkafirongo.paymentservice.model.PaymentRequest;
import com.bkafirongo.paymentservice.model.PaymentResponse;
import com.bkafirongo.paymentservice.repository.TransactionDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionDetailsServiceImp implements TransactionDetailsService {

    private final TransactionDetailsRepository transactionDetailsRepository;

    @Override
    public long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording Payment Details : {}", paymentRequest);

        TransactionDetails transactionDetails = TransactionDetails
                .builder()
                .orderId(paymentRequest.getOrderId())
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .referenceNumber(paymentRequest.getPaymentReference())
                .amount(paymentRequest.getAmount())
                .build();
        transactionDetails = transactionDetailsRepository.save(transactionDetails);
        log.info("Transaction completed with ID : {}", transactionDetails.getId());

        return transactionDetails.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailsByOrderId(Long id) {
        log.info("Getting Payment Details for Order id: {}", id);

        TransactionDetails transactionDetails = transactionDetailsRepository.findByOrderId(id)
                .orElseThrow(
                        () -> new CustomerException("Payment details not found for the given order Id", "ORDER_NOT_FOUND", 404)
                );

        PaymentResponse paymentResponse = PaymentResponse
                .builder()
                .amount(transactionDetails.getAmount())
                .paymentId(transactionDetails.getId())
                .paymentMode(PaymentMode.valueOf(transactionDetails.getPaymentMode()))
                .paymentDate(transactionDetails.getPaymentDate())
                .orderId(transactionDetails.getOrderId())
                .status(transactionDetails.getPaymentStatus())
                .build();
        return paymentResponse;
    }
}
