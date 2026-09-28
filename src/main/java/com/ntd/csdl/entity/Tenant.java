package com.ntd.csdl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @Column(name = "tenant_id", length = 20)
    private String tenantId;

    @Column(name = "cccd", length = 20, unique = true)
    private String cccd;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "permanent_address", length = 200)
    private String permanentAddress;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;
}