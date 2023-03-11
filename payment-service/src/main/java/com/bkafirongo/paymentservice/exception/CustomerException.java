package com.bkafirongo.paymentservice.exception;

import lombok.Data;

@Data
public class CustomerException extends RuntimeException{

    private String errorCode;
    private int status;

    public CustomerException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
