package com.example.fitmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.fitmanager.entity.MembershipStatus;
import com.example.fitmanager.entity.MembershipType;
import com.example.fitmanager.entity.PaymentStatus;


public class MembershipResponse {

    // Fields

    private Long id;

    private Long memberId;

    private MembershipType membershipType;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal amount;

    private MembershipStatus status;

    private Boolean active;

    private BigDecimal totalPaid;
    private BigDecimal outstandingAmount;
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    // ---------------------------------------------------------------

    public MembershipResponse() {}

    public MembershipResponse(final Long id, final Long memberId, //
            final MembershipType membershipType, final LocalDate startDate, //
            final LocalDate endDate, final BigDecimal amount, final MembershipStatus status, //
            final Boolean active, final BigDecimal totalPaid, final BigDecimal outstandingAmount, //
            final PaymentStatus paymentStatus, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt) {

        this.id = id;

        this.memberId = memberId;

        this.membershipType = membershipType;

        this.startDate = startDate;
        this.endDate = endDate;

        this.amount = amount;

        this.status = status;

        this.active = active;

        this.totalPaid = totalPaid;
        this.outstandingAmount = outstandingAmount;
        this.paymentStatus = paymentStatus;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters
    // ---------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
