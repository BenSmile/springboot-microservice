package com.bkafirongo.orderservice.service;

import com.bkafirongo.orderservice.entity.Order;
import com.bkafirongo.orderservice.exception.CustomerException;
import com.bkafirongo.orderservice.external.client.PaymentService;
import com.bkafirongo.orderservice.external.client.ProductService;
import com.bkafirongo.orderservice.external.request.PaymentRequest;
import com.bkafirongo.orderservice.external.response.PaymentResponse;
import com.bkafirongo.orderservice.model.OrderRequest;
import com.bkafirongo.orderservice.model.OrderResponse;
import com.bkafirongo.orderservice.model.PaymentMode;
import com.bkafirongo.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@SpringBootTest
class OrderServiceImplTest {

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

        when(orderRepository.findById(anyLong()))
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

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        CustomerException customerException = assertThrows(
                CustomerException.class, () -> orderService.getOrderDetails(1L)
        );

        assertEquals("ORDER_NOT_FOUND", customerException.getErrorCode());

        assertEquals(404, customerException.getStatus());

        verify(orderRepository, times(1)).findById(anyLong());

    }


    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success() {

        Order order = getMockedOrder();

        OrderRequest orderRequest = getMockedOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2))
                .save(any(Order.class));

        verify(productService, times(1))
                .reduceQuantity(anyLong(), anyLong());

        verify(paymentService, times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(), orderId);
    }


    @DisplayName("Place Order - Payment Failed Scenario")
    @Test
    void test_When_Place_Payment_Fails_then_Order_Placed() {

        Order order = getMockedOrder();

        OrderRequest orderRequest = getMockedOrderRequest();

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(productService.reduceQuantity(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository, times(2))
                .save(any(Order.class));

        verify(productService, times(1))
                .reduceQuantity(anyLong(), anyLong());

        verify(paymentService, times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(), orderId);

        assertEquals("FAILED", order.getOrderStatus());
    }

    private PaymentRequest getMockPaymentRequest() {
        return PaymentRequest
                .builder()
                .orderId(1)
                .amount(100)
                .paymentMode(PaymentMode.CASH)
                .paymentReference("1")
                .build();
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

    private OrderRequest getMockedOrderRequest() {
        return OrderRequest
                .builder()
                .paymentMode(PaymentMode.CASH)
                .productId(1)
                .totalAmount(100)
                .quantity(10)
                .build();
    }


}