package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.MembershipCreateRequest;
import com.example.fitmanager.dto.MembershipResponse;


public interface MembershipService {

    MembershipResponse createMembership(MembershipCreateRequest request);

    MembershipResponse getMembershipById(Long id);

    List<MembershipResponse> getAllMemberships();

    List<MembershipResponse> getMembershipsByMemberId(Long memberId);

    MembershipResponse getActiveMembershipByMemberId(Long memberId);

    void activateMembership(Long id);

    void deactivateMembership(Long id);
}
