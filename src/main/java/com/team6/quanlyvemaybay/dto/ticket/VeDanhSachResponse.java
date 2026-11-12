package com.team6.quanlyvemaybay.dto.ticket;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO riêng cho màn hình quản lý vé, không chứa giá gốc hoặc dữ liệu tài khoản. */
public record VeDanhSachResponse(Long id, String maVe, String tenNguoiDi, String maDatCho,
                                 String maChuyenBay, String maSanBayDi, String maSanBayDen,
                                 LocalDate ngayDi, BigDecimal tongGia, TrangThaiVe trangThai) {
}
