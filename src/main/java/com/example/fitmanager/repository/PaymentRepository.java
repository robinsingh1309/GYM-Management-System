package com.example.fitmanager.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fitmanager.entity.Payment;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMemberId(Long memberId);

    List<Payment> findByMembershipId(Long membershipId);


    @Query("SELECT COALESCE(SUM(p.amount), 0) " //
            + "FROM Payment p " //
            + "WHERE p.membership.id = :membershipId")
    BigDecimal sumAmountByMembershipId(@Param("membershipId") Long membershipId);

}
