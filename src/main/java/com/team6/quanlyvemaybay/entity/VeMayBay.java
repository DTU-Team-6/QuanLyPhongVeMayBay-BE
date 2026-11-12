package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.ChieuBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
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
 * Entity ánh xạ bảng ve_may_bay (Vé máy bay chi tiết theo từng chiều).
 */
@Entity
@Table(name = "ve_may_bay")
public class VeMayBay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_ve", length = 20, nullable = false, unique = true)
    private String maVe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dat_cho_id", nullable = false)
    private DatCho datCho;

    @Column(name = "hanh_khach_id", nullable = false)
    private Long hanhKhachId;

    @Column(name = "ho_ten_nguoi_di", length = 100)
    private String hoTenNguoiDi;

    @Column(name = "email_thanh_toan", length = 100)
    private String emailThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hang_ve_id", nullable = false)
    private HangVeChuyenBay hangVe;

    @Column(name = "goi_hanh_ly_id")
    private Integer goiHanhLyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chieu_bay", nullable = false)
    private ChieuBay chieuBay = ChieuBay.CHIEU_DI;

    @Column(name = "so_ghe", length = 10)
    private String soGhe;

    @Column(name = "gia_goc", precision = 12, scale = 2, nullable = false)
    private BigDecimal giaGoc;

    @Column(name = "gia_ban", precision = 12, scale = 2, nullable = false)
    private BigDecimal giaBan;

    @Column(name = "thue_phi", precision = 12, scale = 2, nullable = false)
    private BigDecimal thuePhi = BigDecimal.ZERO;

    @Column(name = "cuoc_hanh_ly", precision = 12, scale = 2, nullable = false)
    private BigDecimal cuocHanhLy = BigDecimal.ZERO;

    @Column(name = "tong_gia", precision = 12, scale = 2, nullable = false)
    private BigDecimal tongGia;

    @Column(name = "da_in_ve", nullable = false)
    private Boolean daInVe = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiVe trangThai = TrangThaiVe.CHO_THANH_TOAN;

    public VeMayBay() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public DatCho getDatCho() {
        return datCho;
    }

    public void setDatCho(DatCho datCho) {
        this.datCho = datCho;
    }

    public Long getHanhKhachId() {
        return hanhKhachId;
    }

    public void setHanhKhachId(Long hanhKhachId) {
        this.hanhKhachId = hanhKhachId;
    }

    public String getHoTenNguoiDi() {
        return hoTenNguoiDi;
    }

    public void setHoTenNguoiDi(String hoTenNguoiDi) {
        this.hoTenNguoiDi = hoTenNguoiDi;
    }

    public String getEmailThanhToan() {
        return emailThanhToan;
    }

    public void setEmailThanhToan(String emailThanhToan) {
        this.emailThanhToan = emailThanhToan;
    }

    public HangVeChuyenBay getHangVe() {
        return hangVe;
    }

    public void setHangVe(HangVeChuyenBay hangVe) {
        this.hangVe = hangVe;
    }

    public Integer getGoiHanhLyId() {
        return goiHanhLyId;
    }

    public void setGoiHanhLyId(Integer goiHanhLyId) {
        this.goiHanhLyId = goiHanhLyId;
    }

    public ChieuBay getChieuBay() {
        return chieuBay;
    }

    public void setChieuBay(ChieuBay chieuBay) {
        this.chieuBay = chieuBay;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(String soGhe) {
        this.soGhe = soGhe;
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

    public BigDecimal getCuocHanhLy() {
        return cuocHanhLy;
    }

    public void setCuocHanhLy(BigDecimal cuocHanhLy) {
        this.cuocHanhLy = cuocHanhLy;
    }

    public BigDecimal getTongGia() {
        return tongGia;
    }

    public void setTongGia(BigDecimal tongGia) {
        this.tongGia = tongGia;
    }

    public Boolean getDaInVe() {
        return daInVe;
    }

    public void setDaInVe(Boolean daInVe) {
        this.daInVe = daInVe;
    }

    public TrangThaiVe getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiVe trangThai) {
        this.trangThai = trangThai;
    }
}
