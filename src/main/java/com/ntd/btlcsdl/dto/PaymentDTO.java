package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private  Long ordeId;
    private String paymentCode;
    private String method;
    private double amount;
    private String status;
    private String transactionCode;
    private Date pairAt;
    private Date createAt;
}
