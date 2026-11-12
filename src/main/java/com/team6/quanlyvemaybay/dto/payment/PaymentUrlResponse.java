package com.team6.quanlyvemaybay.dto.payment;

import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;

import java.math.BigDecimal;

public record PaymentUrlResponse(
        String paymentUrl,
        String maGiaoDich,
        Long datChoId,
        String maDatCho,
        BigDecimal soTienVnd,
        BigDecimal soTienUsd,
        PhuongThucThanhToan phuongThuc
) {}
