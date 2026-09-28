package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {

    @Id
    @Column(name = "rule_id", length = 20)
    private String ruleId;

    @Column(name = "violation_name", length = 100)
    private String violationName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prescribed_fine", precision = 18, scale = 2)
    private BigDecimal prescribedFine;
}