package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {

    private Long incidentId;
    private String incidentContent;
    private LocalDateTime reportDate;
    private String status;

    private Long roomId;
}