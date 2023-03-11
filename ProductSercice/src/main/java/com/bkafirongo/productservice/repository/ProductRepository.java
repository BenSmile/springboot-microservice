package com.bkafirongo.productservice.repository;

import com.bkafirongo.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
