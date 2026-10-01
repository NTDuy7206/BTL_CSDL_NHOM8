package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationDTO {

    private Long violationId;
    private String violationName;
    private LocalDateTime violationDate;

    private Long roomId;
    private Long ruleId;

    private String detailedDescription;
    private BigDecimal actualFine;
}