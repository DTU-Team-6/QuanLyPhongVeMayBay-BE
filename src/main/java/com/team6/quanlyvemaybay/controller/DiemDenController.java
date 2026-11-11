package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.common.ApiResponse;
import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.destination.CreateDestinationRequest;
import com.team6.quanlyvemaybay.dto.destination.DestinationResponse;
import com.team6.quanlyvemaybay.service.DiemDenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cấp API lấy danh sách và thêm mới điểm đến du lịch (màn hình 99.7.3).
 */
@RestController
@RequestMapping("/api/v1/destinations")
@Tag(name = "Điểm đến", description = "API lấy danh sách (phân trang, tìm kiếm) và thêm mới điểm đến du lịch")
public class DiemDenController {

    private final DiemDenService diemDenService;

    public DiemDenController(DiemDenService diemDenService) {
        this.diemDenService = diemDenService;
    }

    @Operation(
            summary = "Lấy danh sách điểm đến",
            description = "Hỗ trợ tìm kiếm theo từ khóa (tên điểm đến, mã/tên sân bay, thành phố), lọc theo mã sân bay và phân trang."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DestinationResponse>>> getDestinations(
            @Parameter(description = "Từ khóa tìm kiếm theo tên điểm đến, sân bay hoặc thành phố")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Lọc theo mã IATA sân bay (ví dụ: DAD, HAN, SGN)")
            @RequestParam(required = false) String maSanBay,

            @Parameter(description = "Số trang bắt đầu từ 0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Số trang (page) phải lớn hơn hoặc bằng 0")
            int page,

            @Parameter(description = "Số bản ghi mỗi trang (1..100)")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Kích thước trang (size) tối thiểu phải là 1")
            @Max(value = 100, message = "Kích thước trang (size) tối đa không vượt quá 100")
            int size
    ) {
        PageResponse<DestinationResponse> result = diemDenService.getDestinations(keyword, maSanBay, page, size);
        String message = result.content().isEmpty()
                ? "Không tìm thấy điểm đến phù hợp"
                : "Lấy danh sách điểm đến thành công";
        return ResponseEntity.ok(ApiResponse.ok(result, message));
    }

    @Operation(
            summary = "Thêm mới điểm đến",
            description = "Kiểm tra hợp lệ các trường bắt buộc, kiểm tra mã sân bay tồn tại và chống trùng lặp điểm đến trong cùng sân bay."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<DestinationResponse>> createDestination(
            @Valid @RequestBody CreateDestinationRequest request
    ) {
        DestinationResponse created = diemDenService.createDestination(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "Thêm mới điểm đến thành công", created));
    }
}
