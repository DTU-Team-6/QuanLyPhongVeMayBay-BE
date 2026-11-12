package com.team6.quanlyvemaybay.dto.khachhang;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaoKhachHangRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không vượt quá 100 ký tự")
        String email,

        @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự")
        String matKhau,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không vượt quá 100 ký tự")
        String hoTen,

        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$", message = "Số điện thoại không đúng định dạng")
        String soDienThoai,

        @Past(message = "Ngày sinh phải ở trong quá khứ")
        LocalDate ngaySinh,

        GioiTinh gioiTinh,

        @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")
        String diaChi,

        @Size(max = 20, message = "Số giấy tờ không vượt quá 20 ký tự")
        String cmndPassport,

        @Size(max = 50, message = "Quốc tịch không vượt quá 50 ký tự")
        String quocTich,

        @Pattern(regexp = "Member|Silver|Gold|Diamond", message = "Hạng khách hàng không hợp lệ")
        String xepHang,

        @Min(value = 0, message = "Điểm tích lũy không được âm")
        Integer diemTichLuy
) {
}
