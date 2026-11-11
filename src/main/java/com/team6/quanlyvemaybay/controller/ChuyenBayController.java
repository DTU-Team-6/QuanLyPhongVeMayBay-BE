package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.flight.TopCheapFlightResponse;
import com.team6.quanlyvemaybay.dto.flight.FlightSearchResponse;
import com.team6.quanlyvemaybay.dto.flight.FlightDetailResponse;
import com.team6.quanlyvemaybay.service.ChuyenBayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import java.util.List;

/**
 * REST Controller cung cấp dữ liệu chuyến bay công khai cho Trang chủ và màn hình tìm kiếm.
 */
@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Chuyến bay", description = "API truy vấn thông tin chuyến bay công khai")
public class ChuyenBayController {

    private final ChuyenBayService chuyenBayService;

    public ChuyenBayController(ChuyenBayService chuyenBayService) {
        this.chuyenBayService = chuyenBayService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightSearchResponse>>> search(
            @RequestParam String maDi, @RequestParam String maDen,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayDi,
            @RequestParam(defaultValue = "1") @Min(1) @Max(9) int soNguoiLon,
            @RequestParam(defaultValue = "0") @Min(0) @Max(9) int soTreEm,
            @RequestParam(required = false) String maHang) {
        var result = chuyenBayService.search(maDi, maDen, ngayDi, soNguoiLon, soTreEm, maHang);
        return ResponseEntity.ok(ApiResponse.ok(result, "Lấy danh sách chuyến bay thành công"));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<FlightDetailResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(chuyenBayService.detail(id), "Lấy chi tiết chuyến bay thành công"));
    }

    @Operation(
            summary = "Lấy Top 10 chuyến bay giá rẻ nhất",
            description = "Trả về tối đa 10 chuyến bay còn khả dụng và chưa cất cánh, sắp xếp theo giá tăng dần."
    )
    @GetMapping("/top-cheap")
    public ResponseEntity<ApiResponse<List<TopCheapFlightResponse>>> getTopCheapFlights(
            @Parameter(description = "Số lượng chuyến bay tối đa trả về (mặc định 10, từ 1 đến 50)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Số lượng chuyến bay tối thiểu phải là 1")
            @Max(value = 50, message = "Số lượng chuyến bay tối đa không vượt quá 50")
            int limit
    ) {
        List<TopCheapFlightResponse> flights = chuyenBayService.getTopCheapFlights(limit);
        String message = flights.isEmpty()
                ? "Hiện không có chuyến bay khả dụng"
                : "Lấy danh sách top chuyến bay giá rẻ thành công";
        return ResponseEntity.ok(ApiResponse.ok(flights, message));
    }
}
