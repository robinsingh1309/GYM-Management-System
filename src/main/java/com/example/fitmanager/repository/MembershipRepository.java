package com.example.fitmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fitmanager.entity.Membership;


@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    List<Membership> findByMemberId(Long memberId);

    List<Membership> findByActive(Boolean active);

    Optional<Membership> //
            findFirstByMemberIdAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateDesc( //
                    Long memberId, LocalDate currentDate, LocalDate currentDate1);

    boolean //
            existsByMemberIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual( //
                    Long memberId, LocalDate endDate, LocalDate startDate);

    Optional<Membership> findFirstByMemberIdOrderByEndDateDesc(Long memberId);

    boolean existsByMemberIdAndStartDateAfter(Long memberId, LocalDate date);
}
