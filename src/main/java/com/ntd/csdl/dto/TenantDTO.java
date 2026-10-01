package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {

    private Long tenantId;
    private String cccd;
    private String fullName;
    private LocalDate dateOfBirth;
    private String permanentAddress;
    private String phoneNumber;
}