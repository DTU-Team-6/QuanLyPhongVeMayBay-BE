package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity ánh xạ bảng san_bay (Danh mục sân bay).
 */
@Entity
@Table(name = "san_bay")
public class SanBay {

    @Id
    @Column(name = "ma_san_bay", length = 10, nullable = false)
    private String maSanBay;

    @Column(name = "ten_san_bay", length = 100, nullable = false)
    private String tenSanBay;

    @Column(name = "thanh_pho", length = 100, nullable = false)
    private String thanhPho;

    @Column(name = "quoc_gia", length = 100, nullable = false)
    private String quocGia;

    public SanBay() {
    }

    public SanBay(String maSanBay, String tenSanBay, String thanhPho, String quocGia) {
        this.maSanBay = maSanBay;
        this.tenSanBay = tenSanBay;
        this.thanhPho = thanhPho;
        this.quocGia = quocGia;
    }

    public String getMaSanBay() {
        return maSanBay;
    }

    public void setMaSanBay(String maSanBay) {
        this.maSanBay = maSanBay;
    }

    public String getTenSanBay() {
        return tenSanBay;
    }

    public void setTenSanBay(String tenSanBay) {
        this.tenSanBay = tenSanBay;
    }

    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }

    public String getQuocGia() {
        return quocGia;
    }

    public void setQuocGia(String quocGia) {
        this.quocGia = quocGia;
    }
}
