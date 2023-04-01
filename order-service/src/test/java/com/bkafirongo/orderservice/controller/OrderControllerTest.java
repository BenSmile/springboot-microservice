package com.bkafirongo.orderservice.controller;

import com.bkafirongo.orderservice.OrderServiceConfig;
import com.bkafirongo.orderservice.entity.Order;
import com.bkafirongo.orderservice.external.response.PaymentResponse;
import com.bkafirongo.orderservice.model.OrderRequest;
import com.bkafirongo.orderservice.model.OrderResponse;
import com.bkafirongo.orderservice.model.PaymentMode;
import com.bkafirongo.orderservice.repository.OrderRepository;
import com.bkafirongo.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.util.StreamUtils.copyToString;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
class OrderControllerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @RegisterExtension
    static WireMockExtension wireMockServer =
            WireMockExtension
                    .newInstance()
                    .options(WireMockConfiguration.wireMockConfig()
                            .port(8080))
                    .build();

    private ObjectMapper objectMapper
            = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @BeforeEach
    void setup() throws IOException {
        getProductDetailsResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() {
        wireMockServer.stubFor(get(urlMatching("/products/reduce-quantity/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        wireMockServer.stubFor(
                get(urlMatching("/payments/.*"))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(copyToString(
                                        getClass().getClassLoader().getResourceAsStream("mock/GetPayment.json"), defaultCharset())
                                ))
        );
    }

    private void doPayment() {
        // POST /payments
        wireMockServer.stubFor(post(urlEqualTo("/payments"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                ));
    }

    private void getProductDetailsResponse() throws IOException {
        // GET /product/1
        wireMockServer.stubFor(
                get("/products/1")
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(copyToString(
                                        getClass()
                                                .getClassLoader()
                                                .getResourceAsStream("mock/GetProduct.json"), defaultCharset())
                                ))
        );
    }


    @Test
    void test_WhenPlaceOrder_DoPayment_Success() throws Exception {
        // First Place order
        // Get Order by Order Id from DB and check
        // Check output

        OrderRequest orderRequest = getMockOrderRequest();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/place-order")
                        .with(
                                jwt()
                                        .authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest))
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String orderId = mvcResult.getResponse().getContentAsString();

        Optional<Order> byId = orderRepository.findById(Long.valueOf(orderId));

        assertTrue(byId.isPresent());

        Order order = byId.get();

        assertEquals(Long.valueOf(orderId), order.getId());

        assertEquals(order.getOrderStatus(), "PLACED");

        assertEquals(orderRequest.getTotalAmount(), order.getAmount());

    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest
                .builder()
                .paymentMode(PaymentMode.CASH)
                .productId(1)
                .totalAmount(100)
                .quantity(10)
                .build();
    }


    @Test
    void test_WhenPlaceOrder_WithWrongAcces_ThenThrow403() throws Exception {
        var orderRequest = getMockOrderRequest();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/orders/place-order")
                        .with(
                                jwt()
                                        .authorities(new SimpleGrantedAuthority("Admin")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderRequest))
                ).andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    public void test_WhenGetOrder_IsSucces() throws Exception {

        MvcResult mvcResult = mockMvc.
                perform(MockMvcRequestBuilders.get("/orders/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String actualResponse = mvcResult.getResponse().getContentAsString();

        Order order = orderRepository.findById(1L).get();

        String expectedResponse = getOrderResponse(order);

        assertEquals(expectedResponse, actualResponse);

    }

    private String getOrderResponse(Order order) throws IOException {

        PaymentResponse paymentResponse = objectMapper.readValue(
                copyToString(
                        OrderControllerTest.class.getClassLoader().getResourceAsStream("mock/GetPayment.json")
                        , defaultCharset()
                ), PaymentResponse.class);


        OrderResponse.ProductDetails productDetails =
                objectMapper.readValue(
                        copyToString(
                                OrderControllerTest.class.getClassLoader().getResourceAsStream("mock/GetProduct.json")
                                , defaultCharset()
                        ), OrderResponse.ProductDetails.class);

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentResponse)
                .build();
        return objectMapper.writeValueAsString(orderResponse);
    }



    @Test
    public void test_WhenGetOrder_NotFound() throws Exception {

        MvcResult mvcResult = mockMvc.
                perform(MockMvcRequestBuilders.get("/orders/2")
                        .with(jwt().authorities(new SimpleGrantedAuthority("Customer")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

}