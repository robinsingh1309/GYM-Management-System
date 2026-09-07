package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.MembershipPricingCreateRequest;
import com.example.fitmanager.dto.MembershipPricingResponse;
import com.example.fitmanager.entity.MembershipType;


public interface MembershipPricingService {

    MembershipPricingResponse createPricing(MembershipPricingCreateRequest request);

    MembershipPricingResponse getPricingById(Long id);

    List<MembershipPricingResponse> getAllPricing();

    List<MembershipPricingResponse> getPricingByMembershipType(MembershipType membershipType);

    List<MembershipPricingResponse> getActivePricing();

    void activatePricing(Long id);

    void deactivatePricing(Long id);
}
