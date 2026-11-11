package com.team6.quanlyvemaybay.dto.news;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import jakarta.validation.constraints.Size;

/**
 * Request payload cho API cập nhật tin tức (Ảnh 2).
 */
public record UpdateNewsRequest(
        @Size(max = 255, message = "Tiêu đề không vượt quá 255 ký tự")
        String tieuDe,

        String moTaNgan,

        String noiDung,

        String anhDaiDien,

        String chuyenMuc,

        LoaiTinTuc loaiTin,

        TrangThaiTinTuc trangThai
) {
    public LoaiTinTuc resolveLoaiTin() {
        if (loaiTin != null) {
            return loaiTin;
        }
        if (chuyenMuc != null && !chuyenMuc.isBlank()) {
            return LoaiTinTuc.fromString(chuyenMuc);
        }
        return null;
    }
}
