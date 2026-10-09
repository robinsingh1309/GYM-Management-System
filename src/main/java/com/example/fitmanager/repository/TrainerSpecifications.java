package com.example.fitmanager.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.entity.Trainer;
import com.example.fitmanager.entity.TrainerStatus;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;


public final class TrainerSpecifications {

    // Constructors
    // -------------------------------------------------------

    private TrainerSpecifications() {
        // Utility class
    }


    // Methods
    // -------------------------------------------------------

    public static Specification<Trainer> withFilter(final TrainerFilter filter) {

        return (root, query, criteriaBuilder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (Objects.isNull(filter.getStatus())) {
                predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            } else if (filter.getStatus() == TrainerStatus.DELETED) {
                predicates.add(criteriaBuilder.isTrue(root.get("deleted")));
            } else {
                predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
                predicates.add(criteriaBuilder.equal(root.get("active"), //
                        filter.getStatus() == TrainerStatus.ACTIVE));
            }

            if (Objects.nonNull(filter.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), //
                        "%" + filter.getName().toLowerCase(Locale.ROOT) + "%"));
            }
            if (Objects.nonNull(filter.getSpecializations())) {
                predicates.add(root.joinSet("specializations", JoinType.INNER) //
                        .in(filter.getSpecializations()));
                query.distinct(true);
            }
            if (Objects.nonNull(filter.getJoiningDateFrom())) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo( //
                        root.get("joiningDate"), filter.getJoiningDateFrom()));
            }
            if (Objects.nonNull(filter.getJoiningDateTo())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo( //
                        root.get("joiningDate"), filter.getJoiningDateTo()));
            }
            if (Objects.nonNull(filter.getMinExperienceYears())) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo( //
                        root.get("experienceYears"), filter.getMinExperienceYears()));
            }
            if (Objects.nonNull(filter.getMaxExperienceYears())) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo( //
                        root.get("experienceYears"), filter.getMaxExperienceYears()));
            }
            if (Objects.nonNull(filter.getHasLinkedUser())) {
                predicates.add(Boolean.TRUE.equals(filter.getHasLinkedUser()) //
                        ? criteriaBuilder.isNotNull(root.get("user")) //
                        : criteriaBuilder.isNull(root.get("user")));
            }
            if (Objects.nonNull(filter.getUserId())) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filter.getUserId()));
            }
            if (Objects.nonNull(filter.getEmail())) {
                predicates.add(criteriaBuilder.equal(root.get("email"), filter.getEmail()));
            }
            if (Objects.nonNull(filter.getPhoneNumber())) {
                predicates.add(criteriaBuilder.equal(root.get("phoneNumber"), filter.getPhoneNumber()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
