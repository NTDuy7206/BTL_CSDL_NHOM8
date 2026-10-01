package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleDTO {

    private Long ruleId;
    private String violationName;
    private String description;
    private BigDecimal prescribedFine;
}