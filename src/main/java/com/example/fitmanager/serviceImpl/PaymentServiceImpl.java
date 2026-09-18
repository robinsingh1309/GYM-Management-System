package com.example.fitmanager.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fitmanager.dto.PaymentCreateRequest;
import com.example.fitmanager.dto.PaymentResponse;
import com.example.fitmanager.entity.Member;
import com.example.fitmanager.entity.Membership;
import com.example.fitmanager.entity.Payment;
import com.example.fitmanager.entity.PaymentMode;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MemberRepository;
import com.example.fitmanager.repository.MembershipRepository;
import com.example.fitmanager.repository.PaymentRepository;
import com.example.fitmanager.service.PaymentService;


@Service
public class PaymentServiceImpl implements PaymentService {

    // Fields

    private final PaymentRepository paymentRepository;

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;


    // Constructors
    // ------------------------------------------------------

    @Autowired
    public PaymentServiceImpl(final PaymentRepository paymentRepository, //
            final MemberRepository memberRepository, //
            final MembershipRepository membershipRepository) {

        this.paymentRepository = paymentRepository;

        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
    }


    // Methods
    // ------------------------------------------------------

    @Override
    @Transactional
    public PaymentResponse createPayment(final PaymentCreateRequest request) {

        final BigDecimal paymentAmount = request.getAmount();
        final LocalDate paymentDate = request.getPaymentDate();
        final PaymentMode paymentMode = request.getPaymentMode();

        final Long memberId = request.getMemberId();
        final Member member = memberRepository.findById(memberId) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Member not found with id: " + memberId) //
                );

        final Long memberMembershipID = request.getMembershipId();
        if (memberMembershipID == null) {
            throw new BadRequestException("Membership id is required");
        }

        final Membership membership = membershipRepository.findById(memberMembershipID) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership not found with id: " + memberMembershipID) //
                );

        if (!membership.getMember().getId().equals(member.getId())) {
            throw new BadRequestException("Membership does not belong to the specified member");
        }

        final BigDecimal totalPaid = //
                paymentRepository.sumAmountByMembershipId(memberMembershipID);

        final BigDecimal outstandingAmount = //
                membership.getAmount() //
                        .subtract(totalPaid);

        final LocalDate today = LocalDate.now();
        if (paymentDate.isAfter(today)) {
            throw new BadRequestException("Payment date cannot be in the future");
        }

        final LocalDate membershipEndDate = membership.getEndDate();
        if (membershipEndDate.isBefore(today) && //
                paymentAmount.compareTo(outstandingAmount) != 0) {
            throw new BadRequestException( //
                    "For an expired membership, payment must equal the full outstanding balance");
        }

        if (outstandingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Membership is already fully PAID");
        }

        if (paymentAmount.compareTo(outstandingAmount) > 0) {
            throw new BadRequestException("Payment amount cannot exceed outstanding balance");
        }

        final Payment payment = new Payment(member, membership, paymentAmount, paymentDate, paymentMode);
        final Payment savedPayment = paymentRepository.save(payment);

        return this.toDTO(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(final Long id) {

        final Payment payment = paymentRepository.findById(id).orElseThrow( //
                () -> new ResourceNotFoundException("Payment not found with id: " + id) //
        );

        return toDTO(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll().stream() //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByMemberId(final Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        return paymentRepository.findByMemberId(memberId).stream() //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByMembershipId(final Long membershipId) {

        if (!membershipRepository.existsById(membershipId)) {
            throw new ResourceNotFoundException("Membership not found with id: " + membershipId);
        }

        return paymentRepository.findByMembershipId(membershipId).stream() //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }


    // Private Methods
    // ------------------------------------------------------

    private PaymentResponse toDTO(final Payment payment) {

        return new PaymentResponse( //
                payment.getId(), payment.getMember().getId(), //
                payment.getMembership().getId(), payment.getAmount(), //
                payment.getPaymentDate(), payment.getPaymentMode(), //
                payment.getCreatedAt(), //
                payment.getUpdatedAt() //
        );
    }
}
