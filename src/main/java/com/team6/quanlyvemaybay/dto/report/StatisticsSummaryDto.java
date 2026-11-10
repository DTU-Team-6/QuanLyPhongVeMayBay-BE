package com.team6.quanlyvemaybay.dto.report;

import java.math.BigDecimal;

/**
 * Tổng hợp các chỉ số thống kê trong toàn khoảng thời gian truy vấn.
 */
public record StatisticsSummaryDto(
        long tongSoDonDatCho,
        long tongSoVeBanRa,
        BigDecimal tongGiaGoc,
        BigDecimal tongDoanhThu,
        BigDecimal tongLoiNhuan,
        long soChuyenBay,
        long tongSoGhe,
        long soGheDaLapDay,
        BigDecimal tyLeLapDayPhanTram
) {
}
