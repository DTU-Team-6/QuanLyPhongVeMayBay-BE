package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "phien_lam_moi")
public class PhienLamMoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nguoi_dung_id", nullable = false)
    private Long nguoiDungId;

    @Column(name = "nhom_phien", nullable = false, length = 36)
    private String nhomPhien;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "het_han", nullable = false)
    private Instant hetHan;

    @Column(name = "da_su_dung", nullable = false)
    private boolean daSuDung;

    @Column(name = "da_thu_hoi", nullable = false)
    private boolean daThuHoi;

    @Column(name = "ghi_nho", nullable = false)
    private boolean ghiNho;

    protected PhienLamMoi() {
    }

    public PhienLamMoi(Long nguoiDungId, String nhomPhien, String tokenHash, Instant hetHan, boolean ghiNho) {
        this.nguoiDungId = nguoiDungId;
        this.nhomPhien = nhomPhien;
        this.tokenHash = tokenHash;
        this.hetHan = hetHan;
        this.ghiNho = ghiNho;
    }

    public Long getNguoiDungId() { return nguoiDungId; }
    public String getNhomPhien() { return nhomPhien; }
    public Instant getHetHan() { return hetHan; }
    public boolean isDaSuDung() { return daSuDung; }
    public boolean isDaThuHoi() { return daThuHoi; }
    public boolean isGhiNho() { return ghiNho; }
    public void danhDauDaSuDung() { daSuDung = true; }
}
