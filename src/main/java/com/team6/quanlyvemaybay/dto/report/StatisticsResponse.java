package com.team6.quanlyvemaybay.dto.report;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO kết quả trả về cho API GET /api/v1/statistics.
 */
public record StatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        TieuChiThongKe periodType,
        String maHang,
        String tienTe,
        StatisticsSummaryDto summary,
        List<StatisticsPeriodDto> items
) {
}
