package com.team6.quanlyvemaybay.dto.payment;

import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentUrlRequest(
        @NotNull(message = "Mã đặt chỗ (datChoId) không được để trống")
        Long datChoId,

        PhuongThucThanhToan phuongThuc,

        String bankCode,

        String returnUrl
) {}
