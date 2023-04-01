package com.bkafirongo.orderservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

@TestConfiguration
public class OrderServiceConfig {

    public ServiceInstanceListSupplier supplier(){
        return new TestServiceInstanceListSupplier();
    }
}
