package com.ntd.csdl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReportStatusDTO {
    @NotBlank
    private String status;
}
