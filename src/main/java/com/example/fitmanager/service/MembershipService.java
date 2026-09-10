package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.MembershipResponse;
import com.example.fitmanager.dto.MembershipWithPaymentCreateRequest;


public interface MembershipService {

    MembershipResponse createMembership(MembershipWithPaymentCreateRequest request);

    MembershipResponse getMembershipById(Long id);

    List<MembershipResponse> getAllMemberships();

    List<MembershipResponse> getMembershipsByMemberId(Long memberId);

    MembershipResponse getActiveMembershipByMemberId(Long memberId);

    void activateMembership(Long id);

    void deactivateMembership(Long id);
}
