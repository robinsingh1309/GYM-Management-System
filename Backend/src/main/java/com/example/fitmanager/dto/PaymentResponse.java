package com.example.fitmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.fitmanager.entity.PaymentMode;


public class PaymentResponse {

    // Fields

    private Long id;

    private Long memberId;
    private Long membershipId;

    private BigDecimal amount;
    private LocalDate paymentDate;

    private PaymentMode paymentMode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    // --------------------------------------------------------------------------------------------

    public PaymentResponse(final Long id, final Long memberId, final Long membershipId, //
            final BigDecimal amount, final LocalDate paymentDate, final PaymentMode paymentMode, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt) {

        this.id = id;

        this.memberId = memberId;
        this.membershipId = membershipId;

        this.amount = amount;
        this.paymentDate = paymentDate;

        this.paymentMode = paymentMode;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters
    // --------------------------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
