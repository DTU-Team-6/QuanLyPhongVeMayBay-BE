package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.PhuongThucThanhToan;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiThanhToan;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity ánh xạ bảng thanh_toan (Giao dịch thanh toán cổng thanh toán / tiền mặt).
 */
@Entity
@Table(name = "thanh_toan")
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_giao_dich", nullable = false, unique = true, length = 50)
    private String maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dat_cho_id", nullable = false)
    private DatCho datCho;

    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc", nullable = false, length = 30)
    private PhuongThucThanhToan phuongThuc = PhuongThucThanhToan.PAYPAL;

    @Column(name = "so_tien", nullable = false, precision = 14, scale = 2)
    private BigDecimal soTien;

    @Column(name = "thoi_gian_thanh_toan", nullable = false)
    private LocalDateTime thoiGianThanhToan = LocalDateTime.now();

    @Column(name = "ma_giao_dich_doi_tac", length = 100)
    private String maGiaoDichDoiTac;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false, length = 30)
    private TrangThaiThanhToan trangThai = TrangThaiThanhToan.CHO_XU_LY;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    public ThanhToan() {
    }

    public ThanhToan(String maGiaoDich, DatCho datCho, PhuongThucThanhToan phuongThuc,
                     BigDecimal soTien, TrangThaiThanhToan trangThai) {
        this.maGiaoDich = maGiaoDich;
        this.datCho = datCho;
        this.phuongThuc = phuongThuc;
        this.soTien = soTien;
        this.trangThai = trangThai;
        this.thoiGianThanhToan = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public DatCho getDatCho() {
        return datCho;
    }

    public void setDatCho(DatCho datCho) {
        this.datCho = datCho;
    }

    public PhuongThucThanhToan getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(PhuongThucThanhToan phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return thoiGianThanhToan;
    }

    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) {
        this.thoiGianThanhToan = thoiGianThanhToan;
    }

    public String getMaGiaoDichDoiTac() {
        return maGiaoDichDoiTac;
    }

    public void setMaGiaoDichDoiTac(String maGiaoDichDoiTac) {
        this.maGiaoDichDoiTac = maGiaoDichDoiTac;
    }

    public TrangThaiThanhToan getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiThanhToan trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
