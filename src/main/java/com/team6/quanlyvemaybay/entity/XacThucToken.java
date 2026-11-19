package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "xac_thuc_token")
public class XacThucToken {
    public enum Loai { XAC_THUC_EMAIL, QUEN_MAT_KHAU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_token", nullable = false)
    private Loai loaiToken;

    @Column(name = "da_su_dung", nullable = false)
    private boolean daSuDung;

    @Column(name = "het_han", nullable = false)
    private Instant hetHan;

    protected XacThucToken() {
    }

    public XacThucToken(Long userId, String tokenHash, Loai loaiToken, Instant hetHan) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.loaiToken = loaiToken;
        this.hetHan = hetHan;
    }

    public Long getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Loai getLoaiToken() { return loaiToken; }
    public boolean isDaSuDung() { return daSuDung; }
    public Instant getHetHan() { return hetHan; }
}
