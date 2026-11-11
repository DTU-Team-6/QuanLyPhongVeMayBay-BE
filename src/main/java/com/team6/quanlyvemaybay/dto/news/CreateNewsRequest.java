package com.team6.quanlyvemaybay.dto.news;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload cho API thêm tin tức mới (Ảnh 1).
 */
public record CreateNewsRequest(
        @NotBlank(message = "Tiêu đề tin tức không được để trống")
        @Size(max = 255, message = "Tiêu đề không vượt quá 255 ký tự")
        String tieuDe,

        @NotBlank(message = "Mô tả ngắn không được để trống")
        String moTaNgan,

        @NotBlank(message = "Nội dung bài viết không được để trống")
        String noiDung,

        String anhDaiDien,

        String chuyenMuc,

        LoaiTinTuc loaiTin,

        Long nguoiDungId,

        TrangThaiTinTuc trangThai
) {
    public LoaiTinTuc resolveLoaiTin() {
        if (loaiTin != null) {
            return loaiTin;
        }
        if (chuyenMuc != null && !chuyenMuc.isBlank()) {
            LoaiTinTuc resolved = LoaiTinTuc.fromString(chuyenMuc);
            if (resolved != null) {
                return resolved;
            }
        }
        return LoaiTinTuc.KHUYEN_MAI;
    }
}
