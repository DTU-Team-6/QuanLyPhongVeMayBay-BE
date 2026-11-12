package com.team6.quanlyvemaybay.dto.nhanvien;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ThemNhanVienRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "[A-Za-z0-9]+") String maNhanVien,
        @NotBlank @Size(max = 40) @Pattern(regexp = "[\\p{L} ]+") String hoTen,
        @NotNull @Past LocalDate ngaySinh,
        @NotNull GioiTinh gioiTinh,
        @NotBlank @Email @Size(max = 40) String email,
        @NotBlank @Size(min = 8, max = 20) String matKhau,
        @NotBlank @Pattern(regexp = "[0-9]{10,13}") String soDienThoai,
        @NotNull VaiTro vaiTro
) {
}
