package com.team6.quanlyvemaybay.entity.enums;

/**
 * Phân loại tin tức hiển thị trên menu và trang quản lý website.
 */
public enum LoaiTinTuc {
    KHUYEN_MAI("Khuyến mãi"),
    THONG_BAO("Thông tin chuyến bay"),
    QUY_DINH_HANH_LY("Kinh nghiệm bay"),
    GIOI_THIEU("Điểm đến");

    private final String tenHienThi;

    LoaiTinTuc(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public static LoaiTinTuc fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String clean = value.trim();
        for (LoaiTinTuc type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.getTenHienThi().equalsIgnoreCase(clean)) {
                return type;
            }
        }
        // Trường hợp so khớp mềm các từ khóa giao diện
        String lower = clean.toLowerCase();
        if (lower.contains("khuyến mãi") || lower.contains("khuyen mai")) return KHUYEN_MAI;
        if (lower.contains("chuyến bay") || lower.contains("thông tin") || lower.contains("thong bao")) return THONG_BAO;
        if (lower.contains("kinh nghiệm") || lower.contains("hành lý") || lower.contains("quy dinh")) return QUY_DINH_HANH_LY;
        if (lower.contains("điểm đến") || lower.contains("diem den") || lower.contains("giới thiệu")) return GIOI_THIEU;

        return null;
    }
}
