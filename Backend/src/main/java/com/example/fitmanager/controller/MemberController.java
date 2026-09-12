package com.example.fitmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.MemberCreateRequest;
import com.example.fitmanager.dto.MemberResponse;
import com.example.fitmanager.service.MemberService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    // Fields

    private final MemberService memberService;


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }


    // End Points
    // -------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<MemberResponse> createMember( //
            @Valid @RequestBody final MemberCreateRequest request) {

        final MemberResponse response = memberService.createMember(request);

        return ResponseEntity //
                .status(HttpStatus.CREATED) //
                .body(response);
    }

    // GET

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {

        final List<MemberResponse> response = memberService.getAllMembers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<MemberResponse>> getMembersByActiveStatus( //
            @RequestParam("active") final Boolean active) {

        final List<MemberResponse> response = memberService.getMembersByActiveStatus(active);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MemberResponse>> searchMembersByName( //
            @RequestParam("name") final String name) {

        final List<MemberResponse> response = memberService.searchMembersByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember( //
            @PathVariable("id") final Long id) {

        final MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.ok(response);
    }

    // PUT

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember( //
            @PathVariable("id") final Long id, //
            @Valid @RequestBody final MemberCreateRequest request) {

        final MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(response);
    }

    // PATCH

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateMember(@PathVariable("id") final Long id) {

        memberService.activateMember(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateMember(@PathVariable("id") final Long id) {

        memberService.deactivateMember(id);
        return ResponseEntity.noContent().build();
    }

}
