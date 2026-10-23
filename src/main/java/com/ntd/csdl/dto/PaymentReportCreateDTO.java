package com.ntd.csdl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReportCreateDTO {
    @NotBlank(message = "Vui lòng chọn hóa đơn")
    private String invoiceId;

    @Size(max = 100, message = "Mã giao dịch không được vượt quá 100 ký tự")
    @NotBlank(message = "Vui lòng nhập mã giao dịch hoặc nội dung chuyển khoản")
    private String transactionReference;
}
