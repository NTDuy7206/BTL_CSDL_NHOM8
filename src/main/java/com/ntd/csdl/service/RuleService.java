package com.ntd.csdl.service;

import com.ntd.csdl.entity.Rule;
import com.ntd.csdl.repo.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

    // CREATE
    @CacheEvict(
            value = {
                    "rules",
                    "ruleById"
            },
            allEntries = true
    )
    public Rule create(Rule rule) {

        if (rule.getRuleId() == null ||
                rule.getRuleId().isBlank()) {

            throw new RuntimeException(
                    "Rule ID không được để trống"
            );
        }

        if (rule.getPrescribedFine() != null &&
                rule.getPrescribedFine()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Mức phạt không được âm"
            );
        }

        return ruleRepository.save(rule);
    }

    // READ - lấy tất cả quy định
    @Cacheable(value = "rules")
    public List<Rule> getAll() {
        return ruleRepository.findAll();
    }

    // READ - lấy quy định theo ID
    @Cacheable(value = "ruleById", key = "#id")
    public Rule getById(String id) {

        return ruleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        ));
    }

    // UPDATE
    @CacheEvict(
            value = {
                    "rules",
                    "ruleById"
            },
            allEntries = true
    )
    public Rule update(String id, Rule rule) {

        Rule existing = getById(id);

        if (rule.getPrescribedFine() != null &&
                rule.getPrescribedFine()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Mức phạt không được âm"
            );
        }

        existing.setViolationName(rule.getViolationName());
        existing.setDescription(rule.getDescription());
        existing.setPrescribedFine(rule.getPrescribedFine());

        return ruleRepository.save(existing);
    }

    // DELETE
    @CacheEvict(
            value = {
                    "rules",
                    "ruleById"
            },
            allEntries = true
    )
    public void delete(String id) {

        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException(
                    "Quy định không tồn tại"
            );
        }

        ruleRepository.deleteById(id);
    }
}