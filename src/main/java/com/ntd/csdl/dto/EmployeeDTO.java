package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private String employeeId;
    private String cccd;
    private String fullName;
    private String position;
    private BigDecimal baseSalary;
    private Integer seniority;
    private LocalDate dateOfBirth;
    private String phoneNumber;
}