package com.ntd.csdl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ContractRequestCreateDTO {
    @jakarta.validation.Valid @NotNull private RentalDTO.Person tenant;
    @NotNull private LocalDate requestedEndDate;
    @NotBlank(message = "Vui lòng chọn phòng")
    private String roomId;

    @NotNull(message = "Vui lòng chọn ngày dự kiến bắt đầu thuê")
    private LocalDate requestedStartDate;

    @Size(max = 500, message = "Lời nhắn không được vượt quá 500 ký tự")
    private String message;
}
