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

import com.example.fitmanager.dto.MembershipResponse;
import com.example.fitmanager.dto.MembershipWithPaymentCreateRequest;
import com.example.fitmanager.service.MembershipService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/memberships")
public class MembershipController {

    // Fields

    private final MembershipService membershipService;


    // Constructors
    // ---------------------------------------------------------------------

    @Autowired
    public MembershipController(final MembershipService membershipService) {
        this.membershipService = membershipService;
    }


    // API Endpoints
    // ---------------------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<MembershipResponse> createMembership( //
            @Valid @RequestBody final MembershipWithPaymentCreateRequest request) {

        final MembershipResponse response = membershipService.createMembership(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET

    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getAllMemberships() {

        final List<MembershipResponse> response = membershipService.getAllMemberships();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<MembershipResponse> getActiveMembershipByMemberId( //
            @PathVariable("memberId") final Long memberId) {

        final MembershipResponse response = membershipService.getActiveMembershipByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getMembership( //
            @PathVariable("id") final Long id) {

        final MembershipResponse response = membershipService.getMembershipById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MembershipResponse>> getMembershipsByMemberId( //
            @PathVariable("memberId") final Long memberId) {

        final List<MembershipResponse> response = membershipService.getMembershipsByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    // PATCH

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateMembership(@PathVariable("id") final Long id) {

        membershipService.activateMembership(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateMembership(@PathVariable("id") final Long id) {

        membershipService.deactivateMembership(id);
        return ResponseEntity.noContent().build();
    }
}
