package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResolutionDTO {

    private String resolutionId;

    private LocalDate resolutionDate;

    private String result;

    private Boolean isCompleted;

    private String incidentId;

    private String employeeId;
}