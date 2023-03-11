package com.bkafirongo.paymentservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.Instant;

import static javax.persistence.GenerationType.*;

@Entity
@Table(name = "TRANSACTION_DETAILS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetails {

    @Id
    @GeneratedValue(strategy = AUTO)
    @Column(name="ID")
    private long id;
    @Column(name="ORDER_ID")
    private long orderId;
    @Column(name="PAYMENT_MODE")
    private String paymentMode;
    @Column(name="REFERENCE_NUMBER")
    private String referenceNumber;
    @Column(name="PAYMENT_DATE")
    private Instant paymentDate;
    @Column(name="PAYMENT_STATUS")
    private String paymentStatus;
    @Column(name="AMOUNT")
    private long amount;

}
