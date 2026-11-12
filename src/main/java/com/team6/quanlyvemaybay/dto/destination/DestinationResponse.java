package com.team6.quanlyvemaybay.dto.destination;

/**
 * Response DTO trả thông tin điểm đến kèm thông tin sân bay kết nối.
 */
public record DestinationResponse(
        Long id,
        String tenDiemDen,
        String maSanBay,
        String tenSanBay,
        String thanhPho,
        String quocGia,
        String moTa,
        String hinhAnh
) {
}
