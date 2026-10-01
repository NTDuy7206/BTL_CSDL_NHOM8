package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityReadingDTO {

    private Long readingId;

    private Integer month;
    private Integer year;

    private BigDecimal electricityPrice;
    private BigDecimal waterPrice;

    private BigDecimal electricityStartIndex;
    private BigDecimal electricityEndIndex;

    private BigDecimal waterStartIndex;
    private BigDecimal waterEndIndex;

    private Long roomId;
}