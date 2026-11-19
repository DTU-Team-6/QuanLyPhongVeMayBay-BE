package com.team6.quanlyvemaybay.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Mã khôi phục không được để trống")
        @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "Mã khôi phục không hợp lệ")
        String token,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu mới phải có từ 8 đến 72 ký tự")
        @Pattern(regexp = "(?s)^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).+$",
                message = "Mật khẩu mới cần chữ hoa, chữ thường và số")
        String newPassword
) {
}
