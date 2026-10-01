package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

    private Long contractId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal deposit;

    private Long roomId;
    private Long tenantId;
}