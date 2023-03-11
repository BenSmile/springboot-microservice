package com.bkafirongo.productservice.service;

import com.bkafirongo.productservice.model.ProductRequest;
import com.bkafirongo.productservice.model.ProductResponse;

import java.util.List;

public interface ProductService {
    long addProduct(ProductRequest body);

    ProductResponse findById(Long productId);

    void reduceQuantity(Long productId, Long quantity);

    List<ProductResponse> findAll();
}
