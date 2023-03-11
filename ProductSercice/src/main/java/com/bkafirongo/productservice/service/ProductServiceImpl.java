package com.bkafirongo.productservice.service;

import com.bkafirongo.productservice.entity.Product;
import com.bkafirongo.productservice.exception.ProductServiceCustomerException;
import com.bkafirongo.productservice.model.ProductRequest;
import com.bkafirongo.productservice.model.ProductResponse;
import com.bkafirongo.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest body) {
        log.info("Adding product...");
        Product product = Product.builder()
                .productName(body.getName())
                .price(body.getPrice())
                .quantity(body.getQuantity())
                .build();
        product = productRepository.save(product);
        log.info("Product create ...");
        return product.getProductId();
    }

    @Override
    public ProductResponse findById(Long productId) {
        log.info("Get the Product for product id : {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomerException("Product with given id not found", "PRODUCT_NOT_FOUND"));
        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);
        return productResponse;
    }

    @Override
    public void reduceQuantity(Long productId, Long quantity) {
        log.info("Reduce quantity {} for Product Id : {}", quantity, productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomerException("Product with given id not found", "PRODUCT_NOT_FOUND"));
        if (product.getQuantity() < quantity) {
            throw new ProductServiceCustomerException(
                    "Product does not have sufficient quantity",
                    "INSUFFICIENT_QUANTITY"
            );
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product Quantity updated successfully");
    }

    @Override
    public List<ProductResponse> findAll() {

        List<ProductResponse> products = productRepository.findAll().stream().map(this::convertProductToProductResponse).collect(Collectors.toList());
        return products;
    }

    private ProductResponse convertProductToProductResponse(Product product) {
        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);
        return productResponse;
    }
}
