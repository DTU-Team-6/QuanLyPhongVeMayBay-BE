package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Entity ánh xạ bảng khach_hang (hồ sơ khách hàng gắn với tài khoản).
 */
@Entity
@Table(name = "khach_hang")
public class KhachHang {

    @Id
    @Column(name = "nguoi_dung_id", nullable = false)
    private Long nguoiDungId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiDung;

    @Column(name = "cmnd_passport", length = 20)
    private String cmndPassport;

    @Column(name = "quoc_tich", length = 50, nullable = false)
    private String quocTich = "Việt Nam";

    @Column(name = "xep_hang", length = 20, nullable = false)
    private String xepHang = "Member";

    @Column(name = "diem_tich_luy", nullable = false)
    private Integer diemTichLuy = 0;

    public KhachHang() {
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getCmndPassport() {
        return cmndPassport;
    }

    public void setCmndPassport(String cmndPassport) {
        this.cmndPassport = cmndPassport;
    }

    public String getQuocTich() {
        return quocTich;
    }

    public void setQuocTich(String quocTich) {
        this.quocTich = quocTich;
    }

    public String getXepHang() {
        return xepHang;
    }

    public void setXepHang(String xepHang) {
        this.xepHang = xepHang;
    }

    public Integer getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(Integer diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }
}
