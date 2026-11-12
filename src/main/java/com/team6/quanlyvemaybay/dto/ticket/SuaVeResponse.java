package com.team6.quanlyvemaybay.dto.ticket;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;

public record SuaVeResponse(Long id, String maVe, String hoTenNguoiDi,
                            String emailThanhToan, TrangThaiVe trangThai) {
}
