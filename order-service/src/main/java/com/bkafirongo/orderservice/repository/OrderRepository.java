package com.bkafirongo.orderservice.repository;

import com.bkafirongo.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
