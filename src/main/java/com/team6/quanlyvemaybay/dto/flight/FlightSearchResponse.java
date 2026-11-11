package com.team6.quanlyvemaybay.dto.flight;

import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import java.math.BigDecimal;

public record FlightSearchResponse(
        Long chuyenBayId, Long hangVeId, String maChuyenBay,
        AirlineSummaryDto hangHangKhong, AirportSummaryDto sanBayDi, AirportSummaryDto sanBayDen,
        FlightTimeDto thoiGianKhoiHanh, FlightTimeDto thoiGianHaCanh,
        long thoiLuongBayPhut, LoaiKhoang loaiKhoang,
        BigDecimal giaBan, BigDecimal thuePhi, BigDecimal tongGiaVe,
        String tienTe, int soGheTrong, int hanhLyXachTayKg, int hanhLyKyGuiKg) {
}
