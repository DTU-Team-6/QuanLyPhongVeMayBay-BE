package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng tin_tuc (Tin tức, khuyến mãi, thông báo chung).
 */
@Entity
@Table(name = "tin_tuc")
public class TinTuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nguoi_dung_id", nullable = false)
    private Long nguoiDungId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tin", nullable = false)
    private LoaiTinTuc loaiTin = LoaiTinTuc.KHUYEN_MAI;

    @Column(name = "tieu_de", length = 255, nullable = false)
    private String tieuDe;

    @Column(name = "noi_dung", columnDefinition = "LONGTEXT", nullable = false)
    private String noiDung;

    @Column(name = "anh_dai_dien", length = 255)
    private String anhDaiDien;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiTinTuc trangThai = TrangThaiTinTuc.HIEN_THI;

    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime thoiGianTao;

    public TinTuc() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public LoaiTinTuc getLoaiTin() {
        return loaiTin;
    }

    public void setLoaiTin(LoaiTinTuc loaiTin) {
        this.loaiTin = loaiTin;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public TrangThaiTinTuc getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiTinTuc trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
