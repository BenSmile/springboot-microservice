package com.bkafirongo.orderservice.service;

import com.bkafirongo.orderservice.entity.Order;
import com.bkafirongo.orderservice.exception.CustomerException;
import com.bkafirongo.orderservice.external.client.PaymentService;
import com.bkafirongo.orderservice.external.client.ProductService;
import com.bkafirongo.orderservice.external.request.PaymentRequest;
import com.bkafirongo.orderservice.external.response.PaymentResponse;
import com.bkafirongo.orderservice.model.OrderRequest;
import com.bkafirongo.orderservice.model.OrderResponse;
import com.bkafirongo.orderservice.model.OrderResponse.ProductDetails;
import com.bkafirongo.orderservice.model.PaymentMode;
import com.bkafirongo.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;

    private final PaymentService paymentService;

    private final RestTemplate restTemplate;

    @Override
    public long placeOrder(OrderRequest orderRequest) {

        /*
         *  Order Entity -> Save the data with status order created
         *  Product Service -> Block Products ( Reduce quantity)
         *  Payment Service -> Payments -> Success -> COMPLETED, Else -> Cancelled
         * */

        log.info("Placing Order Request : {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating order with status CREATED");

        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment...");

        PaymentRequest paymentRequest = PaymentRequest
                .builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(order.getAmount())
                .build();

        log.info("Payment request body : {}", paymentRequest);
        String orderStatus;

        try {
            paymentService.doPayment(paymentRequest).getBody();
            log.info("Payment done successfully. Changing the Order status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occured in payment. Changing the Order status to PLACED");
            orderStatus = "FAILED";
        }
        order.setOrderStatus(orderStatus);

        orderRepository.save(order);

        log.info("Order Placed successfully with Order Id : {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(Long orderId) {
        log.info("Getting Order Details with Order Id : {}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new CustomerException("Order not found for the given id", "ORDER_NOT_FOUND", 404));

        log.info("Invoking Product Service to fetch the product for id : {}", order.getProductId());

        ProductDetails productResponse =
                restTemplate.getForObject("http://PRODUCT-SERVICE/products/" + order.getProductId(), ProductDetails.class);

        log.info("Getting payment information from the payment service...");

        PaymentResponse paymentResponse =
                restTemplate.getForObject("http://PAYMENT-SERVICE/payments/by-order/" + order.getId(), PaymentResponse.class);

        return OrderResponse.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .productDetails(productResponse)
                .paymentDetails(paymentResponse)
                .build();
    }
}
