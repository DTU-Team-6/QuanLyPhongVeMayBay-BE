package com.team6.quanlyvemaybay.dto.payment;

import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiThanhToan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCallbackResponse(
        String maGiaoDich,
        String maDatCho,
        BigDecimal soTien,
        PhuongThucThanhToan phuongThuc,
        TrangThaiThanhToan trangThai,
        String maGiaoDichDoiTac,
        String thongBao,
        LocalDateTime thoiGianThanhToan
) {}
