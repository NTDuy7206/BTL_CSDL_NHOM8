package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long userId;
    private Long productId;
    private int rating;
    private String comment;
    private String status;
    private Date createdAt;
    private Date updateAt;
}
