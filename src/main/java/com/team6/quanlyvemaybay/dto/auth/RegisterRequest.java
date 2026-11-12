package com.team6.quanlyvemaybay.dto.auth;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không vượt quá 100 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự")
        String password,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không vượt quá 100 ký tự")
        String hoTen,

        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Số điện thoại không đúng định dạng")
        String soDienThoai,

        @Past(message = "Ngày sinh phải ở trong quá khứ")
        LocalDate ngaySinh,

        GioiTinh gioiTinh
) {
}
