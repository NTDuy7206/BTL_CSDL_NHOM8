package com.ntd.csdl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleaningSupportDTO {

    private Long supportId;
    private LocalDateTime supportDate;
    private String taskContent;
    private Boolean isCompleted;

    private Long roomId;
    private Long employeeId;
}