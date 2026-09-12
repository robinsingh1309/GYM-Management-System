package com.example.fitmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.MembershipPricingCreateRequest;
import com.example.fitmanager.dto.MembershipPricingResponse;
import com.example.fitmanager.entity.MembershipType;
import com.example.fitmanager.service.MembershipPricingService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/membership-pricing")
public class MembershipPricingController {

    // Fields

    private final MembershipPricingService membershipPricingService;


    // Constructors
    // -------------------------------------------------------------

    @Autowired
    public MembershipPricingController( //
            final MembershipPricingService membershipPricingService) {

        this.membershipPricingService = membershipPricingService;
    }


    // API Endpoints
    // -------------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<MembershipPricingResponse> createPricing( //
            @Valid @RequestBody final MembershipPricingCreateRequest request) {

        final MembershipPricingResponse response = //
                membershipPricingService.createPricing(request);

        return ResponseEntity //
                .status(HttpStatus.CREATED) //
                .body(response);
    }

    // GET

    @GetMapping
    public ResponseEntity<List<MembershipPricingResponse>> getAllPricing() {

        final List<MembershipPricingResponse> response = //
                membershipPricingService.getAllPricing();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipPricingResponse> getPricing( //
            @PathVariable("id") final Long id) {

        final MembershipPricingResponse response = //
                membershipPricingService.getPricingById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{membershipType}")
    public ResponseEntity<List<MembershipPricingResponse>> getPricingByMembershipType( //
            @PathVariable("membershipType") final MembershipType membershipType) {

        final List<MembershipPricingResponse> response = //
                membershipPricingService.getPricingByMembershipType(membershipType);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<MembershipPricingResponse>> getActivePricing() {

        final List<MembershipPricingResponse> response = //
                membershipPricingService.getActivePricing();

        return ResponseEntity.ok(response);
    }

    // PATCH

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePricing(@PathVariable("id") final Long id) {

        membershipPricingService.activatePricing(id);
        return ResponseEntity //
                .noContent() //
                .build(); //
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePricing(@PathVariable("id") final Long id) {

        membershipPricingService.deactivatePricing(id);
        return ResponseEntity //
                .noContent() //
                .build(); //
    }
}
