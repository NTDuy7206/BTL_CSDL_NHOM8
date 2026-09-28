package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, String> {

    // Derived Query
    List<Rule> findByViolationNameContainingIgnoreCase(String violationName);

    List<Rule> findByPrescribedFineGreaterThan(BigDecimal amount);

    // JPQL
    @Query("""
        SELECT r
        FROM Rule r
        WHERE r.prescribedFine <= :maxFine
    """)
    List<Rule> findRulesWithFineLessThanOrEqual(BigDecimal maxFine);
}