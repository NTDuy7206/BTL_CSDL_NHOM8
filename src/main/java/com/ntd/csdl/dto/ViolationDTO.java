package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationDTO {

    private String violationId;

    private String violationName;

    private LocalDate violationDate;

    private String roomId;

    private String ruleId;

    private String detailedDescription;

    private BigDecimal actualFine;
}