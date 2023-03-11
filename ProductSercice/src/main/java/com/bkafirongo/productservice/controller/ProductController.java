package com.bkafirongo.productservice.controller;

import com.bkafirongo.productservice.model.ProductRequest;
import com.bkafirongo.productservice.model.ProductResponse;
import com.bkafirongo.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.findAll();
        return new ResponseEntity(products, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Long> addProduct(@RequestBody ProductRequest body) {
        long productId = productService.addProduct(body);
        return new ResponseEntity(productId, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Long productId) {
        ProductResponse productResponse = productService.findById(productId);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/reduce-quantity/{id}")
    public ResponseEntity<Void> reduceQuantity(
            @PathVariable("id") Long productId,
            @RequestParam("quantity") Long quantity) {
        productService.reduceQuantity(productId, quantity);
        return new ResponseEntity(HttpStatus.OK);
    }

}
