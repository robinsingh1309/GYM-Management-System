package com.example.fitmanager.serviceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fitmanager.dto.MembershipPricingCreateRequest;
import com.example.fitmanager.dto.MembershipPricingResponse;
import com.example.fitmanager.entity.MembershipPricing;
import com.example.fitmanager.entity.MembershipType;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MembershipPricingRepository;
import com.example.fitmanager.service.MembershipPricingService;


@Service
public class MembershipPricingServiceImpl implements MembershipPricingService {

    // Fields

    private static final BigDecimal MINIMUM_PRICE = new BigDecimal("500.00");

    private final MembershipPricingRepository membershipPricingRepository;


    // Constructors
    // ------------------------------------------------------------------------

    @Autowired
    public MembershipPricingServiceImpl( //
            final MembershipPricingRepository membershipPricingRepository) {

        this.membershipPricingRepository = membershipPricingRepository;
    }


    // Methods
    // ------------------------------------------------------------------------

    @Override
    @Transactional
    public MembershipPricingResponse createPricing( //
            final MembershipPricingCreateRequest request) {

        final MembershipType membershipType = request.getMembershipType();
        final BigDecimal price = request.getPrice();

        if (price.compareTo(MINIMUM_PRICE) < 0) {
            throw new BadRequestException("Price must be at least 500");
        }

        /*
         * If the same membership type + price already exists, do not create a duplicate record.
         *
         * If the existing record is inactive, reactivate it.
         * 
         * If it is already active, reject because there is NO change.
         */
        final MembershipPricing existingPricing = membershipPricingRepository //
                .findByMembershipTypeAndPrice(membershipType, price) //
                .orElse(null);

        if (existingPricing != null) {

            if (Boolean.TRUE.equals(existingPricing.getActive())) {
                throw new BadRequestException("Pricing with the same membership type and price is already active");
            }

            this.deactivateCurrentActivePricing(membershipType);

            existingPricing.setActive(true);

            final MembershipPricing savedPricing = membershipPricingRepository.save(existingPricing);
            return toDTO(savedPricing);
        }

        /*
         * A new price is being configured.
         * 
         * Any currently active price for the same membership type must become INACTIVE.
         */
        this.deactivateCurrentActivePricing(membershipType);

        final MembershipPricing pricing = new MembershipPricing(membershipType, price);
        final MembershipPricing savedPricing = membershipPricingRepository.save(pricing);

        return this.toDTO(savedPricing);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipPricingResponse getPricingById(final Long id) {

        final MembershipPricing pricing = membershipPricingRepository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership pricing not found with id: " + id) //
                );

        return this.toDTO(pricing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPricingResponse> getAllPricing() {

        return membershipPricingRepository.findAll().stream() //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPricingResponse> getPricingByMembershipType( //
            final MembershipType membershipType) {

        return membershipPricingRepository //
                .findByMembershipType(membershipType) //
                .stream() //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPricingResponse> getActivePricing() {

        return membershipPricingRepository.findAll().stream() //
                .filter( //
                        pricing -> Boolean.TRUE.equals(pricing.getActive()) //
                ) //
                .map(this::toDTO) //
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void activatePricing(final Long id) {

        final MembershipPricing pricing = membershipPricingRepository //
                .findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership pricing not found with id: " + id) //
                );

        if (Boolean.TRUE.equals(pricing.getActive())) {
            throw new BadRequestException("Pricing record is already active");
        }

        this.deactivateCurrentActivePricing(pricing.getMembershipType());

        pricing.setActive(true);
        membershipPricingRepository.save(pricing);
    }

    @Override
    @Transactional
    public void deactivatePricing(final Long id) {

        final MembershipPricing pricing = membershipPricingRepository //
                .findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("Membership pricing not found with id: " + id) //
                );

        if (!Boolean.TRUE.equals(pricing.getActive())) {
            throw new BadRequestException("Pricing record is already inactive");
        }

        pricing.setActive(false);
        membershipPricingRepository.save(pricing);
    }


    // Private Methods
    // ------------------------------------------------------------------------

    private void deactivateCurrentActivePricing( //
            final MembershipType membershipType) {

        membershipPricingRepository.findByMembershipTypeAndActiveTrue(membershipType) //
                .ifPresent( //
                        activePricing -> { //
                            activePricing.setActive(false);
                            membershipPricingRepository.save(activePricing);
                        } //
                );
    }

    private MembershipPricingResponse toDTO( //
            final MembershipPricing pricing) {

        return new MembershipPricingResponse( //
                pricing.getId(), pricing.getMembershipType(), //
                pricing.getPrice(), pricing.getActive(), //
                pricing.getCreatedAt(), pricing.getUpdatedAt() //
        );
    }
}
