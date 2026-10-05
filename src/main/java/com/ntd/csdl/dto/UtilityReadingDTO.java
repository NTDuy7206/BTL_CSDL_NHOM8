package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityReadingDTO {

    private String readingId;

    private Integer month;

    private Integer year;

    private BigDecimal electricityPrice;

    private BigDecimal waterPrice;

    private Integer electricityStartIndex;

    private Integer electricityEndIndex;

    private Integer waterStartIndex;

    private Integer waterEndIndex;

    private String roomId;
}