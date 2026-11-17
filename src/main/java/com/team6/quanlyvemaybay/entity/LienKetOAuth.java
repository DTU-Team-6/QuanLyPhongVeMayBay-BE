package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lien_ket_oauth")
public class LienKetOAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nguoi_dung_id", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "nha_cung_cap", nullable = false, length = 20)
    private String nhaCungCap;

    @Column(name = "ma_dinh_danh", nullable = false, length = 255)
    private String maDinhDanh;

    protected LienKetOAuth() {
    }

    public LienKetOAuth(NguoiDung nguoiDung, String nhaCungCap, String maDinhDanh) {
        this.nguoiDung = nguoiDung;
        this.nhaCungCap = nhaCungCap;
        this.maDinhDanh = maDinhDanh;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }
}
