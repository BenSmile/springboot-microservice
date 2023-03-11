package com.bkafirongo.orderservice.external.request;

import com.bkafirongo.orderservice.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    private long orderId;
    private long amount;
    private PaymentMode paymentMode;
    private String paymentReference;
}