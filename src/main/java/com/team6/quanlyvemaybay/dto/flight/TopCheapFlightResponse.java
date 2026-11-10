package com.team6.quanlyvemaybay.dto.flight;

import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;

import java.math.BigDecimal;

/**
 * DTO công khai cho màn hình Top 10 chuyến bay giá rẻ nhất.
 * Tuyệt đối không chứa giaGoc theo quy định bảo mật SRS.
 */
public record TopCheapFlightResponse(
        Long chuyenBayId,
        String maChuyenBay,
        AirlineSummaryDto hangHangKhong,
        AirportSummaryDto sanBayDi,
        AirportSummaryDto sanBayDen,
        FlightTimeDto thoiGianKhoiHanh,
        FlightTimeDto thoiGianHaCanh,
        long thoiLuongBayPhut,
        LoaiKhoang loaiKhoangReNhat,
        BigDecimal giaBan,
        BigDecimal thuePhi,
        BigDecimal tongGiaVe,
        String tienTe,
        Integer soGheTrong,
        Integer hanhLyXachTayKg,
        Integer hanhLyKyGuiKg,
        TrangThaiChuyenBay trangThai
) {
}
