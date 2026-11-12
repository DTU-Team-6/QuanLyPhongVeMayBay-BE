package com.team6.quanlyvemaybay.dto.flight;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;

public record ChuyenBayCuaToiItem(
        Long chuyenBayId,
        String maChuyenBay,
        String tenHangHangKhong,
        AirportSummaryDto sanBayDi,
        AirportSummaryDto sanBayDen,
        FlightTimeDto thoiGianKhoiHanh,
        FlightTimeDto thoiGianHaCanh,
        TrangThaiChuyenBay trangThaiChuyenBay
) {
}
