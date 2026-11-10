package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.LoaiKhoang;
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

import java.math.BigDecimal;

/**
 * Entity ánh xạ bảng hang_ve_chuyen_bay (Hạng vé và giá theo từng chuyến bay).
 * Lưu ý nghiệp vụ SRS: trường giaGoc tuyệt đối không được trả ra API công khai/khách hàng.
 */
@Entity
@Table(name = "hang_ve_chuyen_bay")
public class HangVeChuyenBay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chuyen_bay_id", nullable = false)
    private ChuyenBay chuyenBay;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_khoang", nullable = false)
    private LoaiKhoang loaiKhoang = LoaiKhoang.PHO_THONG;

    @Column(name = "gia_goc", precision = 12, scale = 2, nullable = false)
    private BigDecimal giaGoc;

    @Column(name = "gia_ban", precision = 12, scale = 2, nullable = false)
    private BigDecimal giaBan;

    @Column(name = "thue_phi", precision = 12, scale = 2, nullable = false)
    private BigDecimal thuePhi = BigDecimal.ZERO;

    @Column(name = "hanh_ly_xach_tay_kg", nullable = false)
    private Integer hanhLyXachTayKg = 10;

    @Column(name = "hanh_ly_ky_gui_kg", nullable = false)
    private Integer hanhLyKyGuiKg = 23;

    @Column(name = "tong_so_ghe", nullable = false)
    private Integer tongSoGhe = 150;

    @Column(name = "so_ghe_trong", nullable = false)
    private Integer soGheTrong = 150;

    public HangVeChuyenBay() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChuyenBay getChuyenBay() {
        return chuyenBay;
    }

    public void setChuyenBay(ChuyenBay chuyenBay) {
        this.chuyenBay = chuyenBay;
    }

    public LoaiKhoang getLoaiKhoang() {
        return loaiKhoang;
    }

    public void setLoaiKhoang(LoaiKhoang loaiKhoang) {
        this.loaiKhoang = loaiKhoang;
    }

    public BigDecimal getGiaGoc() {
        return giaGoc;
    }

    public void setGiaGoc(BigDecimal giaGoc) {
        this.giaGoc = giaGoc;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getThuePhi() {
        return thuePhi;
    }

    public void setThuePhi(BigDecimal thuePhi) {
        this.thuePhi = thuePhi;
    }

    public Integer getHanhLyXachTayKg() {
        return hanhLyXachTayKg;
    }

    public void setHanhLyXachTayKg(Integer hanhLyXachTayKg) {
        this.hanhLyXachTayKg = hanhLyXachTayKg;
    }

    public Integer getHanhLyKyGuiKg() {
        return hanhLyKyGuiKg;
    }

    public void setHanhLyKyGuiKg(Integer hanhLyKyGuiKg) {
        this.hanhLyKyGuiKg = hanhLyKyGuiKg;
    }

    public Integer getTongSoGhe() {
        return tongSoGhe;
    }

    public void setTongSoGhe(Integer tongSoGhe) {
        this.tongSoGhe = tongSoGhe;
    }

    public Integer getSoGheTrong() {
        return soGheTrong;
    }

    public void setSoGheTrong(Integer soGheTrong) {
        this.soGheTrong = soGheTrong;
    }
}
