package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private  Long id;
    private Long cartId;
    private Long productId;
    private int quantity;
    private Date addedAt;
}
