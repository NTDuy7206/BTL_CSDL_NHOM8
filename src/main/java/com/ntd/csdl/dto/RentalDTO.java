package com.ntd.csdl.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
@Data
public class RentalDTO {
    private String tenantId;
    private String accountUsername;
    @NotBlank @Size(max=20) private String roomId;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    @NotNull @DecimalMin("0") @Digits(integer=16,fraction=2) private BigDecimal deposit;
    @Valid @NotNull private Person tenant;
    @Data
    public static class Person {
        @NotBlank @Size(max=50) private String fullName;
        @NotBlank @Size(max=20) private String cccd;
        @NotNull @Past private LocalDate dateOfBirth;
        @NotBlank @Size(max=200) private String permanentAddress;
        @NotBlank @Pattern(regexp="[0-9]{10}",message="Số điện thoại phải có 10 chữ số") private String phoneNumber;
    }
}

