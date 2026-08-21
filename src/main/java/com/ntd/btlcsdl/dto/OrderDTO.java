package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private  Long id;
    private Long userId;
    private Long couponId;
    private String orderCode;
    private double totalAmount;
    private double discountAmount;
    private double shippingFee;
    private double finalAmount;
    private String status;
    private String shippingAddress;
    private String note;
    private Date createAt;
    private Date updateAt;
}
