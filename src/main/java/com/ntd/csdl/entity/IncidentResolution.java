package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "incident_resolutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResolution {

    @Id
    @Column(name = "resolution_id", length = 20)
    private String resolutionId;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "is_completed", length = 10)
    private String isCompleted;

    // FK -> incidents.incident_id
    @ManyToOne
    @JoinColumn(
            name = "incident_id",
            referencedColumnName = "incident_id"
    )
    private Incident incident;

    // FK -> employees.employee_id
    @ManyToOne
    @JoinColumn(
            name = "employee_id",
            referencedColumnName = "employee_id"
    )
    private Employee employee;
}