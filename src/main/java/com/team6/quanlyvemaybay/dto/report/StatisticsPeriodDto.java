package com.team6.quanlyvemaybay.dto.report;

import java.math.BigDecimal;

/**
 * Số liệu thống kê doanh thu, vé bán ra và tỷ lệ lấp đầy chuyến bay trong một kỳ (ngày/tháng/năm).
 */
public record StatisticsPeriodDto(
        String period,
        long soDonDatCho,
        long soVeBanRa,
        BigDecimal tongGiaGoc,
        BigDecimal doanhThu,
        BigDecimal loiNhuan,
        long soChuyenBay,
        long tongSoGhe,
        long soGheDaLapDay,
        BigDecimal tyLeLapDayPhanTram
) {
}
