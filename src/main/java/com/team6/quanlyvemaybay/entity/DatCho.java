package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.LoaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
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
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng dat_cho (Đơn đặt chỗ / PNR).
 */
@Entity
@Table(name = "dat_cho")
public class DatCho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_dat_cho", length = 10, nullable = false, unique = true)
    private String maDatCho;

    @Column(name = "khach_hang_id")
    private Long khachHangId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_id")
    private NhanVien nhanVien;

    @Column(name = "email_nguoi_dat", length = 100, nullable = false)
    private String emailNguoiDat;

    @Column(name = "so_dien_thoai_nguoi_dat", length = 20, nullable = false)
    private String soDienThoaiNguoiDat;

    @Column(name = "ho_ten_nguoi_dat", length = 100, nullable = false)
    private String hoTenNguoiDat;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_chuyen_bay", nullable = false)
    private LoaiChuyenBay loaiChuyenBay = LoaiChuyenBay.MOT_CHIEU;

    @Column(name = "so_nguoi_lon", nullable = false)
    private Integer soNguoiLon = 1;

    @Column(name = "so_tre_em", nullable = false)
    private Integer soTreEm = 0;

    @Column(name = "so_em_be", nullable = false)
    private Integer soEmBe = 0;

    @Column(name = "ngay_dat", nullable = false)
    private LocalDateTime ngayDat;

    @Column(name = "tong_gia_goc", precision = 14, scale = 2, nullable = false)
    private BigDecimal tongGiaGoc = BigDecimal.ZERO;

    @Column(name = "tong_gia_ban", precision = 14, scale = 2, nullable = false)
    private BigDecimal tongGiaBan = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiDatCho trangThai = TrangThaiDatCho.CHO_THANH_TOAN;

    public DatCho() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public String getEmailNguoiDat() {
        return emailNguoiDat;
    }

    public void setEmailNguoiDat(String emailNguoiDat) {
        this.emailNguoiDat = emailNguoiDat;
    }

    public String getSoDienThoaiNguoiDat() {
        return soDienThoaiNguoiDat;
    }

    public void setSoDienThoaiNguoiDat(String soDienThoaiNguoiDat) {
        this.soDienThoaiNguoiDat = soDienThoaiNguoiDat;
    }

    public String getHoTenNguoiDat() {
        return hoTenNguoiDat;
    }

    public void setHoTenNguoiDat(String hoTenNguoiDat) {
        this.hoTenNguoiDat = hoTenNguoiDat;
    }

    public LoaiChuyenBay getLoaiChuyenBay() {
        return loaiChuyenBay;
    }

    public void setLoaiChuyenBay(LoaiChuyenBay loaiChuyenBay) {
        this.loaiChuyenBay = loaiChuyenBay;
    }

    public Integer getSoNguoiLon() {
        return soNguoiLon;
    }

    public void setSoNguoiLon(Integer soNguoiLon) {
        this.soNguoiLon = soNguoiLon;
    }

    public Integer getSoTreEm() {
        return soTreEm;
    }

    public void setSoTreEm(Integer soTreEm) {
        this.soTreEm = soTreEm;
    }

    public Integer getSoEmBe() {
        return soEmBe;
    }

    public void setSoEmBe(Integer soEmBe) {
        this.soEmBe = soEmBe;
    }

    public LocalDateTime getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDateTime ngayDat) {
        this.ngayDat = ngayDat;
    }

    public BigDecimal getTongGiaGoc() {
        return tongGiaGoc;
    }

    public void setTongGiaGoc(BigDecimal tongGiaGoc) {
        this.tongGiaGoc = tongGiaGoc;
    }

    public BigDecimal getTongGiaBan() {
        return tongGiaBan;
    }

    public void setTongGiaBan(BigDecimal tongGiaBan) {
        this.tongGiaBan = tongGiaBan;
    }

    public TrangThaiDatCho getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDatCho trangThai) {
        this.trangThai = trangThai;
    }
}
