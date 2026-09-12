package com.example.fitmanager.dto;

import java.math.BigDecimal;

import com.example.fitmanager.entity.MembershipType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;


public class MembershipPricingCreateRequest {

    // Fields

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "500.00", message = "Price must be at least 500")
    private BigDecimal price;


    // Constructors
    // ------------------------------------------------------------------

    public MembershipPricingCreateRequest() {
        //
    }

    
    // Getters and Setters
    // ------------------------------------------------------------------

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(final MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }
}
