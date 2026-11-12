package com.team6.quanlyvemaybay.dto.ticket;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuaVeRequest(
        @NotBlank(message = "Tên người đi không được để trống")
        @Size(min = 10, max = 50, message = "Tên người đi phải có từ 10 đến 50 ký tự")
        String hoTenNguoiDi,
        @NotBlank(message = "Email thanh toán không được để trống")
        @Email(message = "Email thanh toán không đúng định dạng")
        @Size(max = 100, message = "Email thanh toán không được quá 100 ký tự")
        String emailThanhToan
) {
}
