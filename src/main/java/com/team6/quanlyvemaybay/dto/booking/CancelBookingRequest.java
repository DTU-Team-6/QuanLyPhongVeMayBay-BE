package com.team6.quanlyvemaybay.dto.booking;

import java.util.List;

public record CancelBookingRequest(
        String maDatCho,
        List<String> danhSachMaVe,
        String lyDoHuy
) {
}
