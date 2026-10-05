package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {

    private String incidentId;

    private String incidentContent;

    private LocalDate reportDate;

    private String status;

    private String roomId;
}