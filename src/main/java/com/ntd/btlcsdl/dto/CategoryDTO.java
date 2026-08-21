package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private  String name;
    private   String description;
    private String status;
    private Date createAt;
}
