package com.team6.quanlyvemaybay.dto.khachhang;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;

import java.time.LocalDate;

public record KhachHangResponse(
        Long id,
        String email,
        String hoTen,
        String soDienThoai,
        LocalDate ngaySinh,
        GioiTinh gioiTinh,
        String diaChi,
        String cmndPassport,
        String quocTich,
        String xepHang,
        Integer diemTichLuy,
        Byte trangThai
) {
}
