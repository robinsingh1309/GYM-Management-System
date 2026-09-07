package com.example.fitmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.fitmanager.entity.MembershipType;

import jakarta.validation.constraints.NotNull;


public class MembershipCreateRequest {

    // Fields

    @NotNull(message = "Member id is required")
    private Long memberId;

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private BigDecimal amount;


    // Constructors
    // ----------------------------------------------------------------------

    public MembershipCreateRequest() {}


    // Getters and Setters
    // ----------------------------------------------------------------------

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(final Long memberId) {
        this.memberId = memberId;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(final MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

}
