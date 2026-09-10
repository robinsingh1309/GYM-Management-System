package com.example.fitmanager.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fitmanager.dto.MembershipCreateRequest;
import com.example.fitmanager.dto.MembershipResponse;
import com.example.fitmanager.dto.MembershipWithPaymentCreateRequest;
import com.example.fitmanager.dto.PaymentCreateRequest;
import com.example.fitmanager.entity.Member;
import com.example.fitmanager.entity.Membership;
import com.example.fitmanager.entity.MembershipPricing;
import com.example.fitmanager.entity.MembershipStatus;
import com.example.fitmanager.entity.MembershipType;
import com.example.fitmanager.entity.Payment;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MemberRepository;
import com.example.fitmanager.repository.MembershipPricingRepository;
import com.example.fitmanager.repository.MembershipRepository;
import com.example.fitmanager.repository.PaymentRepository;
import com.example.fitmanager.service.MembershipService;


@Service
public class MembershipServiceImpl implements MembershipService {

    // Fields

    private final static int MINIMUM_MEMBERSHIP_AMOUNT = 500;

    private final MemberRepository memberRepository;

    private final MembershipRepository membershipRepository;
    private final MembershipPricingRepository membershipPricingRepository;

    private final PaymentRepository paymentRepository;


    // Constructors
    // ----------------------------------------------------------

    @Autowired
    public MembershipServiceImpl( //
            final MemberRepository memberRepository, //
            final MembershipRepository membershipRepository, //
            final MembershipPricingRepository membershipPricingRepository, //
            final PaymentRepository paymentRepository) {

        this.memberRepository = memberRepository;

        this.membershipRepository = membershipRepository;
        this.membershipPricingRepository = membershipPricingRepository;

        this.paymentRepository = paymentRepository;
    }


    // Methods
    // ----------------------------------------------------------

    @Override
    @Transactional
    public MembershipResponse createMembership(final MembershipWithPaymentCreateRequest request) {

        final MembershipCreateRequest membershipRequest = request.getMembership();
        final LocalDate membershipStartDate = membershipRequest.getStartDate();
        final Long memberId = membershipRequest.getMemberId();

        final LocalDate today = LocalDate.now();
        if (membershipStartDate.isBefore(today)) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        final LocalDate maximumStartDate = today.plusDays(30);
        if (membershipStartDate.isAfter(maximumStartDate)) {
            throw new BadRequestException("Membership start date cannot be more than 30 days in advance");
        }

        final Member member = memberRepository.findById(memberId) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Member not found with id: " + memberId) //
                );

        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new BadRequestException("Cannot create membership for inactive Member");
        }

        if (membershipStartDate.isBefore(member.getJoiningDate())) {
            throw new BadRequestException("Membership start date cannot be before member joining date");
        }

        final List<Membership> existingMemberships = membershipRepository.findByMemberId(memberId);

        for (final Membership existingMembership : existingMemberships) {

            final Long existingMembershipId = existingMembership.getId();

            final BigDecimal totalPaid = //
                    paymentRepository.sumAmountByMembershipId(existingMembershipId);

            final BigDecimal outstandingAmount = //
                    existingMembership.getAmount() //
                            .subtract(totalPaid);

            if (outstandingAmount.compareTo(BigDecimal.ZERO) > 0) {
                throw new BadRequestException( //
                        "Cannot create membership while an outstanding balance exists on an existing membership");
            }
        }

        final boolean upcomingMembershipExists = membershipRepository //
                .existsByMemberIdAndStartDateAfter(memberId, today);

        if (upcomingMembershipExists) {
            throw new BadRequestException("Member already has an upcoming membership");
        }

        final MembershipType membershipType = membershipRequest.getMembershipType();

        final LocalDate endDate;
        switch (membershipType) {
            case MONTHLY:
                endDate = membershipStartDate.plusMonths(1).minusDays(1);
                break;

            case QUARTERLY:
                endDate = membershipStartDate.plusMonths(3).minusDays(1);
                break;

            case HALF_YEARLY:
                endDate = membershipStartDate.plusMonths(6).minusDays(1);
                break;

            case YEARLY:
                endDate = membershipStartDate.plusYears(1).minusDays(1);
                break;

            default:
                throw new BadRequestException("Invalid membership type");
        }

        final boolean membershipOverlap = //
                membershipRepository.existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual( //
                        memberId, endDate, membershipStartDate //
                );

        if (membershipOverlap) {
            throw new BadRequestException("Membership dates overlap with an existing membership");
        }

        final BigDecimal membershipAmount = this.determineMembershipAmount(membershipRequest);

        final PaymentCreateRequest paymentRequest = request.getPayment();
        this.validateFirstPayment(paymentRequest, memberId, membershipAmount);

        final Membership latestMembership = membershipRepository //
                .findFirstByMemberIdOrderByEndDateDesc(memberId) //
                .orElse(null);

        if (latestMembership != null //
                && !membershipStartDate.isAfter(latestMembership.getEndDate()) //
        ) {

            throw new BadRequestException( //
                    "Membership start date must be after the latest existing membership end date");
        }

        final Membership membership = new Membership(member, membershipType, //
                membershipStartDate, endDate, membershipAmount);
        final Membership savedMembership = membershipRepository.save(membership);

        // Get the first Payment
        final Payment firstMembershipPayment = new Payment();
        firstMembershipPayment.setMember(member);
        firstMembershipPayment.setMembership(savedMembership);
        firstMembershipPayment.setAmount(paymentRequest.getAmount());
        firstMembershipPayment.setPaymentDate(paymentRequest.getPaymentDate());
        firstMembershipPayment.setPaymentMode(paymentRequest.getPaymentMode());

        paymentRepository.save(firstMembershipPayment);

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

    private void validateFirstPayment(final PaymentCreateRequest paymentRequest, //
            final Long membershipMemberId, final BigDecimal membershipAmount) {

        final BigDecimal minimumPayment = new BigDecimal(MINIMUM_MEMBERSHIP_AMOUNT);
        final BigDecimal firstPaymentAmount = paymentRequest.getAmount();

        if (firstPaymentAmount.compareTo(minimumPayment) < 0) {
            throw new BadRequestException("First payment must be at least 500");
        }

        if (firstPaymentAmount.compareTo(membershipAmount) > 0) {
            throw new BadRequestException("First payment cannot exceed membership amount");
        }

        if (paymentRequest.getPaymentDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Payment date cannot be in the future");
        }

        if (!membershipMemberId.equals(paymentRequest.getMemberId())) {
            throw new BadRequestException("Payment member does not match membership member");
        }
    }

}
