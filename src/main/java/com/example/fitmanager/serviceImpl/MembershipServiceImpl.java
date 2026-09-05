package com.example.fitmanager.serviceImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.MembershipCreateRequest;
import com.example.fitmanager.dto.MembershipResponse;
import com.example.fitmanager.entity.Member;
import com.example.fitmanager.entity.Membership;
import com.example.fitmanager.entity.MembershipStatus;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MemberRepository;
import com.example.fitmanager.repository.MembershipRepository;
import com.example.fitmanager.service.MembershipService;


@Service
public class MembershipServiceImpl implements MembershipService {

    // Fields

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;


    // Constructors
    // ----------------------------------------------------------

    @Autowired
    public MembershipServiceImpl( //
            final MembershipRepository membershipRepository, //
            final MemberRepository memberRepository) {

        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
    }


    // Methods
    // ----------------------------------------------------------

    @Override
    public MembershipResponse createMembership(final MembershipCreateRequest request) {

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        final LocalDate endDate;
        switch (request.getMembershipType()) {
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

        final Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (!Boolean.TRUE.equals(member.getActive())) {
            throw new BadRequestException("Cannot create membership for inactive Member");
        }

        final Membership membership = new Membership(member, request.getMembershipType(), //
                request.getStartDate(), endDate, request.getAmount());

        final Membership savedMembership = membershipRepository.save(membership);
        return toDTO(savedMembership);
    }

    @Override
    public MembershipResponse getMembershipById(final Long id) {

        final Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));
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
        return membershipRepository.findByMemberId(memberId).stream().map(this::toDTO).toList();
    }

    @Override
    public MembershipResponse getActiveMembershipByMemberId(final Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        final Membership membership = membershipRepository //
                .findFirstByMemberIdAndActiveTrueAndEndDateGreaterThanEqualOrderByEndDateDesc(memberId, LocalDate.now()) //
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

        final Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

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

        return new MembershipResponse(membership.getId(), membership.getMember().getId(),
                membership.getMembershipType(), membership.getStartDate(), membership.getEndDate(),
                membership.getAmount(), status, membership.getActive(), membership.getCreatedAt(),
                membership.getUpdatedAt());
    }
}
