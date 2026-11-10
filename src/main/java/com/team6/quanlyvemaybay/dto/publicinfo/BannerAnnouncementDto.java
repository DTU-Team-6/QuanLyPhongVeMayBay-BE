package com.team6.quanlyvemaybay.dto.publicinfo;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;

/**
 * Dữ liệu banner khuyến mãi hoặc thông báo chung đang hiển thị trên Trang chủ/Header.
 */
public record BannerAnnouncementDto(
        Long id,
        LoaiTinTuc loaiTin,
        String tieuDe,
        String anhDaiDien,
        String thoiGianTao
) {
}
