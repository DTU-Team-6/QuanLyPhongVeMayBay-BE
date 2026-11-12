package com.team6.quanlyvemaybay.dto.news;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho một thẻ tin tức hiển thị trên danh sách quản trị (Ảnh 3).
 */
public record NewsItemDto(
        Long id,
        String maTin,
        String tieuDe,
        String moTaNgan,
        String anhDaiDien,
        String chuyenMuc,
        LoaiTinTuc loaiTin,
        String ngayDang,
        LocalDateTime thoiGianTao,
        String tacGia,
        TrangThaiTinTuc trangThai
) {
}
