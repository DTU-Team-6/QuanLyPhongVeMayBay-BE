package com.team6.quanlyvemaybay.dto.destination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO nhận dữ liệu màn hình Thêm điểm đến (99.7.3).
 */
public record CreateDestinationRequest(
        @Schema(description = "Tên điểm đến du lịch", example = "Cầu Rồng Đà Nẵng", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Tên điểm đến không được để trống")
        @Size(max = 100, message = "Tên điểm đến không vượt quá 100 ký tự")
        String tenDiemDen,

        @Schema(description = "Mã IATA sân bay lân cận", example = "DAD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Mã sân bay không được để trống")
        @Size(max = 10, message = "Mã sân bay không vượt quá 10 ký tự")
        String maSanBay,

        @Schema(description = "Mô tả giới thiệu điểm đến", example = "Biểu tượng nổi tiếng bên bờ sông Hàn, Đà Nẵng.")
        @Size(max = 5000, message = "Mô tả không vượt quá 5000 ký tự")
        String moTa,

        @Schema(description = "Đường dẫn ảnh minh họa điểm đến", example = "/assets/destinations/caurong.jpg")
        @Size(max = 255, message = "Đường dẫn hình ảnh không vượt quá 255 ký tự")
        String hinhAnh
) {
}
