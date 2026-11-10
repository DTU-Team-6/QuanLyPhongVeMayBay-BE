package com.team6.quanlyvemaybay.dto.report;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO kết quả trả về cho API tạo báo cáo chi tiết (dành riêng cho Nhân viên/Admin).
 */
public record ReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        LoaiBaoCao reportType,
        TieuChiThongKe groupBy,
        TrangThaiDatCho trangThaiDatCho,
        String tienTe,
        ReportSummaryDto summary,
        List<ReportItemDto> items
) {
}
