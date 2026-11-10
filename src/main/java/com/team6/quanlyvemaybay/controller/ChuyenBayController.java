package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.flight.TopCheapFlightResponse;
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
import org.springframework.web.bind.annotation.RestController;

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
