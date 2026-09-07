package com.example.fitmanager.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.MembershipCreateRequest;
import com.example.fitmanager.dto.MembershipResponse;
import com.example.fitmanager.entity.Member;
import com.example.fitmanager.entity.Membership;
import com.example.fitmanager.entity.MembershipPricing;
import com.example.fitmanager.entity.MembershipStatus;
import com.example.fitmanager.entity.MembershipType;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MemberRepository;
import com.example.fitmanager.repository.MembershipPricingRepository;
import com.example.fitmanager.repository.MembershipRepository;
import com.example.fitmanager.service.MembershipService;


@Service
public class MembershipServiceImpl implements MembershipService {

    // Fields

    private final static int MINIMUM_MEMBERSHIP_AMOUNT = 500;

    private final MemberRepository memberRepository;

    private final MembershipRepository membershipRepository;
    private final MembershipPricingRepository membershipPricingRepository;


    // Constructors
    // ----------------------------------------------------------

    @Autowired
    public MembershipServiceImpl( //
            final MemberRepository memberRepository, //
            final MembershipRepository membershipRepository, //
            final MembershipPricingRepository membershipPricingRepository) {

        this.memberRepository = memberRepository;

        this.membershipRepository = membershipRepository;
        this.membershipPricingRepository = membershipPricingRepository;
    }


    // Methods
    // ----------------------------------------------------------

    @Override
    public MembershipResponse createMembership(final MembershipCreateRequest request) {

        final LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        final LocalDate maximumStartDate = today.plusDays(30);
        if (request.getStartDate().isAfter(maximumStartDate)) {
            throw new BadRequestException("Membership start date cannot be more than 30 days in advance");
        }

        final Member member = memberRepository.findById(request.getMemberId()) //
                .orElseThrow(() -> new ResourceNotFoundException( //
                        "Member not found with id: " + request.getMemberId()) //
                );

        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new BadRequestException("Cannot create membership for inactive Member");
        }

        if (request.getStartDate().isBefore(member.getJoiningDate())) {
            throw new BadRequestException("Membership start date cannot be before member joining date");
        }

        final MembershipType membershipType = request.getMembershipType();

        final LocalDate endDate;
        switch (membershipType) {
            case MONTHLY:
                endDate = request.getStartDate().plusMonths(1).minusDays(1);
                break;

            case QUARTERLY:
                endDate = request.getStartDate().plusMonths(3).minusDays(1);
                break;

            case HALF_YEARLY:
                endDate = request.getStartDate().plusMonths(6).minusDays(1);
                break;

            case YEARLY:
                endDate = request.getStartDate().plusYears(1).minusDays(1);
                break;

            default:
                throw new BadRequestException("Invalid membership type");
        }

        final BigDecimal membershipAmount = this.determineMembershipAmount(request);
        final Membership membership = new Membership(member, membershipType, //
                request.getStartDate(), endDate, membershipAmount);

        final Membership savedMembership = membershipRepository.save(membership);
        return toDTO(savedMembership);
    }

    @Override
    public MembershipResponse getMembershipById(final Long id) {

        final Membership membership = membershipRepository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership not found with id: " + id) //
                );
        return toDTO(membership);
    }

    @Override
    public List<MembershipResponse> getAllMemberships() {
        return membershipRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public List<MembershipResponse> getMembershipsByMemberId(final Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }
        return membershipRepository.findByMemberId(memberId).stream() //
                .map(this::toDTO) //
                .toList();
    }

    @Override
    public MembershipResponse getActiveMembershipByMemberId(final Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        final LocalDate currentDate = LocalDate.now();
        final Membership membership = membershipRepository //
                .findFirstByMemberIdAndActiveTrueAndEndDateGreaterThanEqualOrderByEndDateDesc(memberId, currentDate) //
                .orElseThrow(
                        () -> new ResourceNotFoundException("No active membership found for member id: " + memberId) //
                );

        return toDTO(membership);
    }

    @Override
    public void activateMembership(final Long id) {

        final Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        membership.setActive(true);
        membershipRepository.save(membership);
    }

    @Override
    public void deactivateMembership(final Long id) {

        final Membership membership = membershipRepository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership not found with id: " + id) //
                );

        membership.setActive(false);
        membershipRepository.save(membership);
    }


    // Helper Methods
    // ----------------------------------------------------------

    private MembershipResponse toDTO(final Membership membership) {

        final MembershipStatus status;

        if (!Boolean.TRUE.equals(membership.getActive())) {
            status = MembershipStatus.INACTIVE;
        } else if (membership.getEndDate().isBefore(LocalDate.now())) {
            status = MembershipStatus.EXPIRED;
        } else {
            status = MembershipStatus.ACTIVE;
        }

        return new MembershipResponse( //
                membership.getId(), membership.getMember().getId(), //
                membership.getMembershipType(), membership.getStartDate(), //
                membership.getEndDate(), membership.getAmount(), status, //
                membership.getActive(), membership.getCreatedAt(), //
                membership.getUpdatedAt() //
        );
    }

    private BigDecimal determineMembershipAmount(final MembershipCreateRequest request) {

        final Authentication authentication = //
                SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            throw new BadRequestException("Authenticated user is required");
        }

        final boolean isAdmin = authentication.getAuthorities() //
                .stream() //
                .anyMatch( //
                        authority -> //
                        "ROLE_ADMIN".equals(authority.getAuthority()) //
                );

        final boolean isStaff = authentication.getAuthorities() //
                .stream() //
                .anyMatch( //
                        authority -> //
                        "ROLE_STAFF".equals(authority.getAuthority()) //
                );

        if (!isAdmin && !isStaff) {
            throw new BadRequestException("User does not have permission to create membership");
        }

        final MembershipType membershipType = request.getMembershipType();
        final MembershipPricing activePricing = //
                membershipPricingRepository.findByMembershipTypeAndActiveTrue( //
                        membershipType //
                ).orElse(null);

        if (isStaff) {

            if (Objects.isNull(activePricing)) {
                throw new BadRequestException("No active pricing configured for membership type: " + membershipType);
            }

            return activePricing.getPrice();
        }

        final BigDecimal requestedAmount = request.getAmount();
        if (Objects.isNull(requestedAmount)) {
            throw new BadRequestException("Amount is required for ADMIN");
        }

        if (requestedAmount.compareTo(new BigDecimal(MINIMUM_MEMBERSHIP_AMOUNT)) < 0) {
            throw new BadRequestException("Membership amount must be at least 500");
        }

        return requestedAmount;
    }

}
