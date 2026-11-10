package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity ánh xạ bảng hang_hang_khong (Hãng hàng không).
 */
@Entity
@Table(name = "hang_hang_khong")
public class HangHangKhong {

    @Id
    @Column(name = "ma_hang", length = 10, nullable = false)
    private String maHang;

    @Column(name = "ten_hang", length = 100, nullable = false)
    private String tenHang;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiHang trangThai = TrangThaiHang.HOAT_DONG;

    public HangHangKhong() {
    }

    public HangHangKhong(String maHang, String tenHang, String logoUrl, TrangThaiHang trangThai) {
        this.maHang = maHang;
        this.tenHang = tenHang;
        this.logoUrl = logoUrl;
        this.trangThai = trangThai;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getTenHang() {
        return tenHang;
    }

    public void setTenHang(String tenHang) {
        this.tenHang = tenHang;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public TrangThaiHang getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHang trangThai) {
        this.trangThai = trangThai;
    }
}
