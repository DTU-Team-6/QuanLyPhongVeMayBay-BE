package com.team6.quanlyvemaybay.dto.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO đại diện cho một vé đang chờ thanh toán hiển thị trên bảng của giao diện "Thanh toán vé".
 */
public record PendingTicketDto(
        Long veId,
        String maVe,
        Long datChoId,
        String maDatCho,
        String noiDi,
        String maSanBayDi,
        String noiDen,
        String maSanBayDen,
        LocalDateTime ngayDat,
        LocalDateTime ngayBay,
        BigDecimal giaTien,
        String trangThai,
        LocalDateTime hanGiuCho,
        String thongTinHanhLy,
        String tenHanhKhach,
        String soGhe
) {
}
