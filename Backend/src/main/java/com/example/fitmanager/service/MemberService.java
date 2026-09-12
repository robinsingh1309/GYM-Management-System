package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.MemberCreateRequest;
import com.example.fitmanager.dto.MemberResponse;


public interface MemberService {

    MemberResponse createMember(MemberCreateRequest request);

    MemberResponse getMemberById(Long id);

    List<MemberResponse> getAllMembers();

    MemberResponse updateMember(Long id, MemberCreateRequest request);

    void activateMember(Long id);

    void deactivateMember(Long id);

    List<MemberResponse> getMembersByActiveStatus(Boolean active);

    List<MemberResponse> searchMembersByName(String name);
}
