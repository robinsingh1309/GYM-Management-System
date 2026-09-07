package com.example.fitmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.fitmanager.entity.MembershipType;


public class MembershipPricingResponse {

    // Fields

    private Long id;

    private MembershipType membershipType;
    private BigDecimal price;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    // -------------------------------------------

    public MembershipPricingResponse(final Long id, //
            final MembershipType membershipType, final BigDecimal price, //
            final Boolean active, final LocalDateTime createdAt, //
            final LocalDateTime updatedAt) {

        this.id = id;

        this.membershipType = membershipType;
        this.price = price;

        this.active = active;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters and Setters
    // -------------------------------------------

    public Long getId() {
        return id;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public BigDecimal getPrice() {
        return price;
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
