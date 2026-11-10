package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng chuyen_bay (Lịch trình chuyến bay).
 */
@Entity
@Table(name = "chuyen_bay")
public class ChuyenBay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_chuyen_bay", length = 20, nullable = false)
    private String maChuyenBay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hang", nullable = false)
    private HangHangKhong hangHangKhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_bay_di", nullable = false)
    private SanBay sanBayDi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_bay_den", nullable = false)
    private SanBay sanBayDen;

    @Column(name = "thoi_gian_khoi_hanh", nullable = false)
    private LocalDateTime thoiGianKhoiHanh;

    @Column(name = "thoi_gian_ha_canh", nullable = false)
    private LocalDateTime thoiGianHaCanh;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiChuyenBay trangThai = TrangThaiChuyenBay.DUNG_GIO;

    public ChuyenBay() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaChuyenBay() {
        return maChuyenBay;
    }

    public void setMaChuyenBay(String maChuyenBay) {
        this.maChuyenBay = maChuyenBay;
    }

    public HangHangKhong getHangHangKhong() {
        return hangHangKhong;
    }

    public void setHangHangKhong(HangHangKhong hangHangKhong) {
        this.hangHangKhong = hangHangKhong;
    }

    public SanBay getSanBayDi() {
        return sanBayDi;
    }

    public void setSanBayDi(SanBay sanBayDi) {
        this.sanBayDi = sanBayDi;
    }

    public SanBay getSanBayDen() {
        return sanBayDen;
    }

    public void setSanBayDen(SanBay sanBayDen) {
        this.sanBayDen = sanBayDen;
    }

    public LocalDateTime getThoiGianKhoiHanh() {
        return thoiGianKhoiHanh;
    }

    public void setThoiGianKhoiHanh(LocalDateTime thoiGianKhoiHanh) {
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
    }

    public LocalDateTime getThoiGianHaCanh() {
        return thoiGianHaCanh;
    }

    public void setThoiGianHaCanh(LocalDateTime thoiGianHaCanh) {
        this.thoiGianHaCanh = thoiGianHaCanh;
    }

    public TrangThaiChuyenBay getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiChuyenBay trangThai) {
        this.trangThai = trangThai;
    }
}
