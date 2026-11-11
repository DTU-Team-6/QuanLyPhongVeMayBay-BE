package com.team6.quanlyvemaybay.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketItemDto(
        Long id,
        String maVe,
        String maChuyenBay,
        String hangBay,
        String noiDi,
        String maSanBayDi,
        String noiDen,
        String maSanBayDen,
        LocalDateTime thoiGianKhoiHanh,
        LocalDateTime thoiGianHaCanh,
        String chieuBay,
        String loaiKhoang,
        String soGhe,
        String tenHanhKhach,
        BigDecimal giaBan,
        BigDecimal thuePhi,
        BigDecimal cuocHanhLy,
        BigDecimal tongGia,
        String trangThai
) {
}
