package com.example.fitmanager.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.MemberCreateRequest;
import com.example.fitmanager.dto.MemberResponse;
import com.example.fitmanager.entity.Member;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.MemberRepository;
import com.example.fitmanager.service.MemberService;


@Service
public class MemberServiceImpl implements MemberService {

    // Fields

    private final MemberRepository repository;


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public MemberServiceImpl(final MemberRepository repository) {
        this.repository = repository;
    }


    // Methods
    // -------------------------------------------------------

    @Override
    public MemberResponse createMember(final MemberCreateRequest request) {

        final String memberEmail = request.getEmail();
        final String memberPhoneNumber = request.getPhoneNumber();

        if (repository.existsByEmail(memberEmail)) {
            throw new BadRequestException("Email already exists: " + memberEmail);
        }

        if (repository.existsByPhoneNumber(memberPhoneNumber)) {
            throw new BadRequestException("Phone number already exists: " + memberPhoneNumber);
        }

        final Member member = new Member(request.getName(), request.getEmail(), //
                request.getPhoneNumber(), request.getDateOfBirth(), request.getGender(), //
                request.getAddress(), request.getJoiningDate());

        final Member savedMember = repository.save(member);
        return toDTO(savedMember);
    }

    @Override
    public MemberResponse getMemberById(final Long id) {

        final Member member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        return toDTO(member);
    }

    @Override
    public MemberResponse updateMember(final Long id, final MemberCreateRequest request) {

        final Member member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        final String memberEmail = request.getEmail();
        final String memberPhoneNumber = request.getPhoneNumber();

        if (!member.getEmail().equals(memberEmail) && repository.existsByEmail(memberEmail)) {
            throw new BadRequestException("Email already exists: " + memberEmail);
        }

        if (!member.getPhoneNumber().equals(memberPhoneNumber) && repository.existsByPhoneNumber(memberPhoneNumber)) {
            throw new BadRequestException("Phone number already exists: " + memberPhoneNumber);
        }

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhoneNumber(request.getPhoneNumber());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setGender(request.getGender());
        member.setAddress(request.getAddress());
        member.setJoiningDate(request.getJoiningDate());

        final Member updatedMember = repository.save(member);
        return toDTO(updatedMember);
    }

    @Override
    public void activateMember(final Long id) {

        final Member member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setActive(true);
        repository.save(member);
    }

    @Override
    public void deactivateMember(final Long id) {

        final Member member = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setActive(false);
        repository.save(member);
    }

    @Override
    public List<MemberResponse> getMembersByActiveStatus(final Boolean active) {
        return repository.findByActive(active).stream().map(this::toDTO).toList();
    }

    @Override
    public List<MemberResponse> getAllMembers() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public List<MemberResponse> searchMembersByName(final String name) {
        return repository.findByNameContainingIgnoreCase(name).stream().map(this::toDTO).toList();
    }


    // Helper Methods
    // -------------------------------------------------------

    private MemberResponse toDTO(final Member member) {

        return new MemberResponse(member.getId(), member.getName(), //
                member.getEmail(), member.getPhoneNumber(), member.getDateOfBirth(), //
                member.getGender(), member.getAddress(), member.getJoiningDate(), //
                member.getActive(), member.getCreatedAt(), member.getUpdatedAt());
    }
}
