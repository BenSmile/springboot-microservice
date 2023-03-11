package com.bkafirongo.orderservice.external.decoder;

import com.bkafirongo.orderservice.exception.CustomerException;
import com.bkafirongo.orderservice.external.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {

        ObjectMapper objectMapper = new ObjectMapper();

        log.info("url ::{}", response.request().url());
        log.info("headers ::{}", response.request().headers());

        try {
            ErrorResponse errorResponse =
                    objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);

            return new CustomerException(
                    errorResponse.getErrorMessage(),
                    errorResponse.getErrorCode(),
                    response.status());

        } catch (IOException e) {
            throw new CustomerException("Internal error", "INTERNAL_ERROR", 500);
        }
    }
}
