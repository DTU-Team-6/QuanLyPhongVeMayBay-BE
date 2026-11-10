package com.team6.quanlyvemaybay.dto.report;

import java.math.BigDecimal;

/**
 * Một dòng dữ liệu chi tiết trong báo cáo theo hãng, nhân viên hoặc thời gian.
 */
public record ReportItemDto(
        String key,
        String label,
        long soDonDatCho,
        long soVeBanRa,
        BigDecimal tongGiaGoc,
        BigDecimal doanhThu,
        BigDecimal loiNhuan
) {
}
