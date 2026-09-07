package com.example.fitmanager.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fitmanager.entity.MembershipPricing;
import com.example.fitmanager.entity.MembershipType;


@Repository
public interface MembershipPricingRepository extends JpaRepository<MembershipPricing, Long> {

    List<MembershipPricing> findByMembershipType(MembershipType membershipType);

    List<MembershipPricing> findByMembershipTypeAndActive(MembershipType membershipType, Boolean active);

    Optional<MembershipPricing> findByMembershipTypeAndPrice(MembershipType membershipType, BigDecimal price);

    Optional<MembershipPricing> findByMembershipTypeAndActiveTrue(MembershipType membershipType);
}
