package com.ntd.btlcsdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private String full_name;
    private String email;
    private String phone;
    private String address;
    private String Status;
    private Date createdAt;
    private Date updatedAt;
}
