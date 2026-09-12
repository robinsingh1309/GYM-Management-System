package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.PaymentCreateRequest;
import com.example.fitmanager.dto.PaymentResponse;


public interface PaymentService {

    PaymentResponse createPayment(PaymentCreateRequest request);

    PaymentResponse getPaymentById(Long id);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPaymentsByMemberId(Long memberId);

    List<PaymentResponse> getPaymentsByMembershipId(Long membershipId);
}
