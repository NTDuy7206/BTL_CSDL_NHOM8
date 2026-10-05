package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleaningSupportDTO {

    private String supportId;

    private LocalDate supportDate;

    private String taskContent;

    private Boolean isCompleted;

    private String roomId;

    private String employeeId;
}