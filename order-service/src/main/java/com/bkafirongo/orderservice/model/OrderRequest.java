package com.bkafirongo.orderservice.model;

import lombok.Data;

@Data
public class OrderRequest {
    private long productId;
    private long totalAmount;
    private long quantity;
    private PaymentMode paymentMode;
}
