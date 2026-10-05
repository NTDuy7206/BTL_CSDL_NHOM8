package com.ntd.csdl.service;

import com.ntd.csdl.dto.RuleDTO;
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
    public Rule create(RuleDTO dto) {

        if (dto.getRuleId() == null ||
                dto.getRuleId().isBlank()) {

            throw new RuntimeException(
                    "Rule ID không được để trống"
            );
        }

        if (ruleRepository.existsById(dto.getRuleId())) {
            throw new RuntimeException(
                    "Quy định đã tồn tại"
            );
        }

        if (dto.getPrescribedFine() != null &&
                dto.getPrescribedFine()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Mức phạt không được âm"
            );
        }

        Rule rule = new Rule();

        rule.setRuleId(dto.getRuleId());
        rule.setViolationName(dto.getViolationName());
        rule.setDescription(dto.getDescription());
        rule.setPrescribedFine(dto.getPrescribedFine());

        return ruleRepository.save(rule);
    }

    // READ
    @Cacheable(value = "rules")
    public List<Rule> getAll() {
        return ruleRepository.findAll();
    }

    // READ BY ID
    @Cacheable(value = "ruleById", key = "#id")
    public Rule getById(String id) {

        return ruleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        )
                );
    }

    // UPDATE
    @CacheEvict(
            value = {
                    "rules",
                    "ruleById"
            },
            allEntries = true
    )
    public Rule update(String id, RuleDTO dto) {

        Rule existing = getById(id);

        if (dto.getPrescribedFine() != null &&
                dto.getPrescribedFine()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Mức phạt không được âm"
            );
        }

        existing.setViolationName(dto.getViolationName());
        existing.setDescription(dto.getDescription());
        existing.setPrescribedFine(dto.getPrescribedFine());

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