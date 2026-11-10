package com.team6.quanlyvemaybay.dto.report;

import java.math.BigDecimal;

/**
 * Tổng hợp số liệu của toàn bộ kết quả báo cáo (chỉ dùng cho Nhân viên/Admin).
 */
public record ReportSummaryDto(
        long tongSoDonDatCho,
        long tongSoVe,
        BigDecimal tongGiaGoc,
        BigDecimal tongDoanhThu,
        BigDecimal tongLoiNhuan
) {
}
