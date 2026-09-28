package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @Column(name = "incident_id", length = 20)
    private String incidentId;

    @Column(name = "incident_content", columnDefinition = "TEXT")
    private String incidentContent;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "status", length = 50)
    private String status;

    // FK -> rooms.room_id
    @ManyToOne
    @JoinColumn(
            name = "room_id",
            referencedColumnName = "room_id"
    )
    private Room room;
}