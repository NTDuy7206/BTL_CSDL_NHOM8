package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private String roomId;
    private Integer floor;
    private Float area;
    private String roomType;
    private String status;
    private BigDecimal basePrice;
}