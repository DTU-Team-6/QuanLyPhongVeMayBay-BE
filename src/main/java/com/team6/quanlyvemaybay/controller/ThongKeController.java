package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.report.StatisticsFilterRequest;
import com.team6.quanlyvemaybay.dto.report.StatisticsResponse;
import com.team6.quanlyvemaybay.service.BaoCaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cấp số liệu thống kê tổng hợp doanh thu, số vé bán và tỷ lệ lấp đầy chuyến bay theo kỳ.
 */
@RestController
@RequestMapping("/api/v1/statistics")
@Tag(name = "Thống kê", description = "API thống kê tổng hợp doanh thu, lượng vé và tỷ lệ lấp đầy chuyến bay")
public class ThongKeController {

    private final BaoCaoService baoCaoService;

    public ThongKeController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    @Operation(
            summary = "Lấy số liệu thống kê tổng hợp theo kỳ",
            description = "Tính toán doanh thu, số lượng vé bán ra và tỷ lệ lấp đầy chuyến bay theo ngày/tháng/năm."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @Valid @ParameterObject @ModelAttribute StatisticsFilterRequest filter
    ) {
        StatisticsResponse statistics = baoCaoService.getStatistics(filter);
        String message = statistics.items().isEmpty()
                ? "Không có dữ liệu thống kê trong khoảng thời gian đã chọn"
                : "Lấy dữ liệu thống kê thành công";
        return ResponseEntity.ok(ApiResponse.ok(statistics, message));
    }
}
