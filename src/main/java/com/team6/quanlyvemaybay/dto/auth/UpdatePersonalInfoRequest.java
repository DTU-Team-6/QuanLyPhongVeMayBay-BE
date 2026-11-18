package com.team6.quanlyvemaybay.dto.auth;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePersonalInfoRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không vượt quá 100 ký tự")
        String hoTen,

        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Số điện thoại không đúng định dạng")
        String soDienThoai,

        @Past(message = "Ngày sinh phải ở trong quá khứ")
        LocalDate ngaySinh,

        @NotNull(message = "Giới tính không được để trống")
        GioiTinh gioiTinh,

        @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")
        String diaChi,

        @Size(max = 20, message = "Số giấy tờ không vượt quá 20 ký tự")
        String cmndPassport,

        @Pattern(regexp = ".*\\S.*", message = "Quốc tịch không được để trống")
        @Size(max = 50, message = "Quốc tịch không vượt quá 50 ký tự")
        String quocTich
) {
}
