package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDTO {
    private Long id;
    private Long orderId;
    private String trackingNumber;
    private String carrier;
    private String shippingAddress;
    private String status;
    private Date shipppedAt;
    private Date deliveredAt;
}
