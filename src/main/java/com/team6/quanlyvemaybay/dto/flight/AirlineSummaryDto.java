package com.team6.quanlyvemaybay.dto.flight;

/**
 * Thông tin tóm tắt hãng hàng không hiển thị trên card chuyến bay.
 */
public record AirlineSummaryDto(
        String maHang,
        String tenHang,
        String logoUrl
) {
}
