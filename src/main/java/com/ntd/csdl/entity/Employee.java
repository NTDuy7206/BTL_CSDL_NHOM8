package com.ntd.csdl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(name = "employee_id", length = 20)
    private String employeeId;

    @Column(name = "cccd", length = 20, unique = true)
    private String cccd;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "base_salary", precision = 18, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "seniority")
    private Integer seniority;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;
}