package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private  Long id;
    private   String productName;
    private  String productDescription;
    private double productPrice;
    private  Long productCategoryId;
    private  Long supplierId;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}
