package com.team6.quanlyvemaybay.dto.flight;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TraCuuChuyenBayCuaToiRequest(
        @NotBlank(message = "Mã đặt chỗ không được để trống")
        @Size(max = 10, message = "Mã đặt chỗ không vượt quá 10 ký tự")
        String maDatCho,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không vượt quá 100 ký tự")
        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Số điện thoại không đúng định dạng")
        String soDienThoai
) {
}
