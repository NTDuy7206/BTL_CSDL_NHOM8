package com.ntd.csdl.service;

import com.ntd.csdl.entity.Rule;
import com.ntd.csdl.repo.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

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

    public List<Rule> getAll() {
        return ruleRepository.findAll();
    }

    public Rule getById(String id) {

        return ruleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quy định"
                        ));
    }

    public void delete(String id) {

        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException(
                    "Quy định không tồn tại"
            );
        }

        ruleRepository.deleteById(id);
    }
}