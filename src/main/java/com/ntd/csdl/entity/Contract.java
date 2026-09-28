package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @Column(name = "contract_id", length = 20)
    private String contractId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "deposit", precision = 18, scale = 2)
    private BigDecimal deposit;

    // FK -> rooms.room_id
    @ManyToOne
    @JoinColumn(
            name = "room_id",
            referencedColumnName = "room_id"
    )
    private Room room;

    // FK -> tenants.tenant_id
    @ManyToOne
    @JoinColumn(
            name = "tenant_id",
            referencedColumnName = "tenant_id"
    )
    private Tenant tenant;
}