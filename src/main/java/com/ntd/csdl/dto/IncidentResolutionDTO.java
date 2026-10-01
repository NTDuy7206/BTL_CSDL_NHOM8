package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResolutionDTO {

    private Long resolutionId;
    private LocalDateTime resolutionDate;
    private String result;
    private Boolean isCompleted;

    private Long incidentId;
    private Long employeeId;
}