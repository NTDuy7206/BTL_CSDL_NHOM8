package com.ntd.csdl.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "cleaning_supports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleaningSupport {

    @Id
    @Column(name = "support_id", length = 20)
    private String supportId;

    @Column(name = "support_date")
    private LocalDate supportDate;

    @Column(name = "task_content", columnDefinition = "TEXT")
    private String taskContent;

    @Column(name = "is_completed", length = 10)
    private String isCompleted;

    // FK -> rooms.room_id
    @ManyToOne
    @JoinColumn(
            name = "room_id",
            referencedColumnName = "room_id"
    )
    private Room room;

    // FK -> employees.employee_id
    @ManyToOne
    @JoinColumn(
            name = "employee_id",
            referencedColumnName = "employee_id"
    )
    private Employee employee;
}