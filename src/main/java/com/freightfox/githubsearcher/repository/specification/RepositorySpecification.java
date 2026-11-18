package com.freightfox.githubsearcher.repository.specification;

import com.freightfox.githubsearcher.entity.StoredRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RepositorySpecification {

    private RepositorySpecification() {
        // Utility class
    }

    public static Specification<StoredRepository> withFilters(String language, Integer minStars) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(language)) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("language")),
                    language.trim().toLowerCase()
                ));
            }

            if (minStars != null && minStars >= 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stars"), minStars));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
