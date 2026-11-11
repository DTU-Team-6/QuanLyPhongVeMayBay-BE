package com.team6.quanlyvemaybay.dto.booking;

public record CancelBookingResponse(
        boolean thanhCong,
        String maDatCho,
        int soVeDaHuy,
        String thongBao
) {
}
