package com.team6.quanlyvemaybay.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tổng hợp toàn bộ dữ liệu cần thiết cho màn hình "Thanh toán vé" của Front-end.
 */
public record PendingPaymentScreenDto(
        String maDatCho,
        LocalDateTime hanGiuCho,
        String thongBaoGiuCho,
        BigDecimal tongTien,
        int tongSoVe,
        List<PendingTicketDto> danhSachVe
) {
}
