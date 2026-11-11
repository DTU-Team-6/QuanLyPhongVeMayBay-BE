package com.team6.quanlyvemaybay.dto.booking;

import java.time.LocalDate;

public record PassengerItemDto(
        Long id,
        String hoTen,
        LocalDate ngaySinh,
        String gioiTinh,
        String loaiHanhKhach,
        String cmndPassport,
        String soDienThoai,
        String email
) {
}
