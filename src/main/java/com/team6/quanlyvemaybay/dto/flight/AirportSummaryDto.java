package com.team6.quanlyvemaybay.dto.flight;

/**
 * Thông tin tóm tắt sân bay đi/đến.
 */
public record AirportSummaryDto(
        String maSanBay,
        String tenSanBay,
        String thanhPho,
        String quocGia
) {
}
