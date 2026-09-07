package com.example.fitmanager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;


@Entity
@Table(name = "membership_pricing")
public class MembershipPricing extends BaseEntity {

    // Fields

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, length = 20)
    private MembershipType membershipType;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    private Boolean active = true;


    // Constructors
    // -----------------------------------------------------------------

    public MembershipPricing() {
        //
    }

    public MembershipPricing(final MembershipType membershipType, //
            final BigDecimal price) {

        this.membershipType = membershipType;
        this.price = price;

        this.active = true;
    }


    // Getters and Setters
    // -----------------------------------------------------------------

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

}
