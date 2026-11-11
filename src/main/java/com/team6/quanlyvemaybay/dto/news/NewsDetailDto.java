package com.team6.quanlyvemaybay.dto.news;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;

import java.time.LocalDateTime;

/**
 * DTO chi tiết bài viết tin tức phục vụ màn hình chỉnh sửa hoặc xem chi tiết (Ảnh 2).
 */
public record NewsDetailDto(
        Long id,
        String maTin,
        String tieuDe,
        String moTaNgan,
        String noiDung,
        String anhDaiDien,
        String chuyenMuc,
        LoaiTinTuc loaiTin,
        Long nguoiDungId,
        String tacGia,
        String ngayDang,
        LocalDateTime thoiGianTao,
        TrangThaiTinTuc trangThai
) {
}
