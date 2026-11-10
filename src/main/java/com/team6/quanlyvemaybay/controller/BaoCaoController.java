package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.report.ReportFilterRequest;
import com.team6.quanlyvemaybay.dto.report.ReportResponse;
import com.team6.quanlyvemaybay.service.BaoCaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller xử lý lọc và xuất dữ liệu báo cáo chi tiết (Admin / Nhân viên).
 */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Báo cáo", description = "API tạo báo cáo doanh thu theo hãng, nhân viên và thời gian")
public class BaoCaoController {

    private final BaoCaoService baoCaoService;

    public BaoCaoController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    @Operation(
            summary = "Tạo báo cáo chi tiết qua JSON body",
            description = "Nhận điều kiện lọc từ ngày - đến ngày, loại báo cáo, tiêu chí gom nhóm và trả về dữ liệu báo cáo."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody ReportFilterRequest filter
    ) {
        ReportResponse report = baoCaoService.generateReport(filter);
        String message = report.items().isEmpty()
                ? "Không có dữ liệu báo cáo trong khoảng thời gian đã chọn"
                : "Tạo báo cáo thành công";
        return ResponseEntity.ok(ApiResponse.ok(report, message));
    }

    @Operation(
            summary = "Tạo báo cáo chi tiết qua query parameters",
            description = "Hỗ trợ gọi GET kèm query params (startDate, endDate, reportType, groupBy...) cho cùng bộ lọc báo cáo."
    )
    @GetMapping("/generate")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReportByQuery(
            @Valid @ParameterObject @ModelAttribute ReportFilterRequest filter
    ) {
        ReportResponse report = baoCaoService.generateReport(filter);
        String message = report.items().isEmpty()
                ? "Không có dữ liệu báo cáo trong khoảng thời gian đã chọn"
                : "Tạo báo cáo thành công";
        return ResponseEntity.ok(ApiResponse.ok(report, message));
    }
}
