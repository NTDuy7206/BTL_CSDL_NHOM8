package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Column(name = "invoice_id", length = 20)
    private String invoiceId;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "rent_amount", precision = 18, scale = 2)
    private BigDecimal rentAmount;

    @Column(name = "electricity_amount", precision = 18, scale = 2)
    private BigDecimal electricityAmount;

    @Column(name = "water_amount", precision = 18, scale = 2)
    private BigDecimal waterAmount;

    @Column(name = "fine_amount", precision = 18, scale = 2)
    private BigDecimal fineAmount;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // FK -> contracts.contract_id
    @ManyToOne
    @JoinColumn(
            name = "contract_id",
            referencedColumnName = "contract_id"
    )
    private Contract contract;
}