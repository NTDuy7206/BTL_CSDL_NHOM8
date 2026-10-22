package com.ntd.csdl.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileUpdateDTO(
        @Size(max = 120, message = "Tên hiển thị không được vượt quá 120 ký tự")
        String displayName,
        @Email(message = "Email không hợp lệ")
        @Size(max = 190, message = "Email không được vượt quá 190 ký tự")
        String email,
        @Size(max = 30, message = "Số điện thoại không được vượt quá 30 ký tự")
        String phone
) {
}
