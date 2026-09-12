package com.example.fitmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fitmanager.entity.PaymentMode;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


public class PaymentCreateRequest {

    // Fields

    @NotNull(message = "Member id is required")
    private Long memberId;

    private Long membershipId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;


    // Constructors
    // ----------------------------------------------------------------------

    public PaymentCreateRequest() {
        //
    }


    // Getters and Setters
    // ----------------------------------------------------------------------

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(final Long memberId) {
        this.memberId = memberId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(final Long membershipId) {
        this.membershipId = membershipId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(final LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(final PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

}
