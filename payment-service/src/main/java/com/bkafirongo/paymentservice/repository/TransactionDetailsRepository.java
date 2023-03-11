package com.bkafirongo.paymentservice.repository;

import com.bkafirongo.paymentservice.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionDetailsRepository extends JpaRepository<TransactionDetails, Long> {
    Optional<TransactionDetails> findByOrderId(Long orderId);
}
