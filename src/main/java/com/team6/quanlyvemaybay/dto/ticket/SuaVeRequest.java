package com.team6.quanlyvemaybay.dto.ticket;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuaVeRequest(
        @NotBlank @Size(min = 10, max = 50) String hoTenNguoiDi,
        @NotBlank @Email @Size(max = 100) String emailThanhToan
) {
}
