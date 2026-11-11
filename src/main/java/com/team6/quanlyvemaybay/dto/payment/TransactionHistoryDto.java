package com.team6.quanlyvemaybay.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionHistoryDto(
        Long datChoId,
        String maDatCho,
        String tuyenBay,
        LocalDate ngayDat,
        LocalDate ngayBay,
        BigDecimal giaTien,
        String trangThai,
        String phuongThucThanhToan,
        String maGiaoDich
) {}
