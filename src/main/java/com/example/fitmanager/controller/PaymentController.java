package com.example.fitmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.PaymentCreateRequest;
import com.example.fitmanager.dto.PaymentResponse;
import com.example.fitmanager.service.PaymentService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    // Fields

    private final PaymentService paymentService;


    // Constructors
    // ------------------------------------------------------------

    @Autowired
    public PaymentController(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    // API Endpoints
    // ------------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment( //
            @Valid @RequestBody final PaymentCreateRequest request) {

        final PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity //
                .status(HttpStatus.CREATED) //
                .body(response);
    }

    // GET

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        final List<PaymentResponse> response = paymentService.getAllPayments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment( //
            @PathVariable("id") final Long id) {

        final PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMemberId( //
            @PathVariable("memberId") final Long memberId) {

        final List<PaymentResponse> response = paymentService.getPaymentsByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/membership/{membershipId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMembershipId( //
            @PathVariable("membershipId") final Long membershipId) {

        final List<PaymentResponse> response = //
                paymentService.getPaymentsByMembershipId(membershipId);

        return ResponseEntity.ok(response);
    }
}
