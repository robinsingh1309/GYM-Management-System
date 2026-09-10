package com.example.fitmanager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


public class MembershipWithPaymentCreateRequest {

    // Fields

    @NotNull(message = "Membership details are required")
    @Valid
    private MembershipCreateRequest membership;

    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentCreateRequest payment;


    // Constructors
    // -------------------------------------------------

    public MembershipWithPaymentCreateRequest() {
        //
    }


    // Getters and Setters
    // -------------------------------------------------

    public MembershipCreateRequest getMembership() {
        return membership;
    }

    public void setMembership(final MembershipCreateRequest membership) {
        this.membership = membership;
    }

    public PaymentCreateRequest getPayment() {
        return payment;
    }

    public void setPayment(final PaymentCreateRequest payment) {
        this.payment = payment;
    }
}
