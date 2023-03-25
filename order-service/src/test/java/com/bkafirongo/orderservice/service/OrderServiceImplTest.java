package com.bkafirongo.orderservice.service;

import com.bkafirongo.orderservice.entity.Order;
import com.bkafirongo.orderservice.exception.CustomerException;
import com.bkafirongo.orderservice.external.client.PaymentService;
import com.bkafirongo.orderservice.external.client.ProductService;
import com.bkafirongo.orderservice.external.response.PaymentResponse;
import com.bkafirongo.orderservice.model.OrderResponse;
import com.bkafirongo.orderservice.model.PaymentMode;
import com.bkafirongo.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@SpringBootTest
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success() {
        // Mocking
        Order order = getMockedOrder();

        when(orderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(order));

        when(restTemplate.getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), OrderResponse.ProductDetails.class))
                .thenReturn(getMockProductResponse());

        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payments/by-order/" + order.getId(), PaymentResponse.class))
                .thenReturn(getMockPaymentResponse());
        // Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1L);
        // Verification
        verify(orderRepository, times(1)).findById(anyLong());

        verify(restTemplate, times(1))
                .getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), OrderResponse.ProductDetails.class);

        verify(restTemplate, times(1))
                .getForObject("http://PAYMENT-SERVICE/payments/by-order/" + order.getId(), PaymentResponse.class);
        // Assert

        assertNotNull(orderResponse);

        assertEquals(order.getId(), orderResponse.getOrderId());


    }

    @DisplayName("Get Order - Failure Scenario")
    @Test
    void test_When_Get_Order_NOT_FOUND_then_Not_Found() {

        when(orderRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        CustomerException customerException = assertThrows(
                CustomerException.class, () -> orderService.getOrderDetails(1L)
        );

        assertEquals("ORDER_NOT_FOUND", customerException.getErrorCode());

        assertEquals(404, customerException.getStatus());

        verify(orderRepository, times(1)).findById(anyLong());

    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse
                .builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .status("ACCEPTED")
                .orderId(1)
                .amount(200)
                .build();
    }

    private OrderResponse.ProductDetails getMockProductResponse() {
        return OrderResponse.ProductDetails
                .builder()
                .productId(2L)
                .productName("iPhone")
                .price(100L)
                .quantity(200L)
                .build();
    }

    private Order getMockedOrder() {
        return Order
                .builder()
                .id(1L)
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .quantity(200)
                .amount(500)
                .productId(2L)
                .build();
    }



}