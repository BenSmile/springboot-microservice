package com.bkafirongo.orderservice.config;

import com.bkafirongo.orderservice.external.decoder.CustomErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    ErrorDecoder errorDecoder(){
        return new CustomErrorDecoder();
    }

}
