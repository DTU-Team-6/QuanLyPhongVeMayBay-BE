package com.team6.quanlyvemaybay.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetailDto(
        Long datChoId,
        String maDatCho,
        String hoTenNguoiDat,
        String emailNguoiDat,
        String soDienThoaiNguoiDat,
        String loaiChuyenBay,
        int soNguoiLon,
        int soTreEm,
        int soEmBe,
        LocalDateTime ngayDat,
        LocalDateTime hanGiuCho,
        BigDecimal tongGiaBan,
        String trangThai,
        List<TicketItemDto> danhSachVe,
        List<PassengerItemDto> danhSachHanhKhach
) {
}
