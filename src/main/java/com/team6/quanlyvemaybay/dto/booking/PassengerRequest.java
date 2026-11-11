package com.team6.quanlyvemaybay.dto.booking;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.LoaiHanhKhach;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PassengerRequest(
        @NotBlank @Size(max = 100) String hoTen,
        @NotNull @Past LocalDate ngaySinh,
        @NotNull GioiTinh gioiTinh,
        @Email String email,
        @Pattern(regexp = "^\\+?[0-9 ]{8,20}$") String soDienThoai,
        @Size(max = 20) String cmndPassport,
        @NotNull LoaiHanhKhach loaiHanhKhach
) {}
