package com.bkafirongo.paymentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    private long orderId;
    private long amount;
    private PaymentMode paymentMode;
    private String paymentReference;
}
