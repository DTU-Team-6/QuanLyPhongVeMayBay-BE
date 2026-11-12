package com.team6.quanlyvemaybay.dto.auth;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;

import java.time.LocalDate;

public record PersonalInfoResponse(
        Long id,
        String email,
        String hoTen,
        String soDienThoai,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String diaChi,
        String anhDaiDien,
        VaiTro vaiTro
) {
}
