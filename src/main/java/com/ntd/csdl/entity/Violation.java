package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "violations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Violation {

    @Id
    @Column(name = "violation_id", length = 20)
    private String violationId;

    @Column(name = "violation_name", length = 100)
    private String violationName;

    @Column(name = "violation_date")
    private LocalDate violationDate;

    // FK -> rooms.room_id
    @ManyToOne
    @JoinColumn(
            name = "room_id",
            referencedColumnName = "room_id"
    )
    private Room room;

    // FK -> rules.rule_id
    @ManyToOne
    @JoinColumn(
            name = "rule_id",
            referencedColumnName = "rule_id"
    )
    private Rule rule;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "actual_fine", precision = 18, scale = 2)
    private BigDecimal actualFine;
}