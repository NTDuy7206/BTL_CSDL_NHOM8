package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private  Long id;
    private Long productId;
    private int quantity;
    private int reservedQuantity;
    private int reoderLevel;
    private Date createAt;
}
