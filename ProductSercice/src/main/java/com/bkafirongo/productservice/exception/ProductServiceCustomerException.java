package com.bkafirongo.productservice.exception;

import lombok.Data;

@Data
public class ProductServiceCustomerException extends RuntimeException{

    private String errorCode;
    public ProductServiceCustomerException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
