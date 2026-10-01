package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long invoiceId;

    private Integer month;
    private Integer year;

    private BigDecimal rentAmount;
    private BigDecimal electricityAmount;
    private BigDecimal waterAmount;
    private BigDecimal fineAmount;
    private BigDecimal totalAmount;

    private Long contractId;
}