package com.team6.quanlyvemaybay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tin_nhan_chat")
public class TinNhanChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_gui_id", nullable = false)
    private NguoiDung nguoiGui;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhan_id", nullable = false)
    private NguoiDung nguoiNhan;

    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "gui_luc", nullable = false)
    private LocalDateTime guiLuc;

    @Column(name = "da_doc", nullable = false)
    private Boolean daDoc = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public NguoiDung getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(NguoiDung nguoiGui) { this.nguoiGui = nguoiGui; }
    public NguoiDung getNguoiNhan() { return nguoiNhan; }
    public void setNguoiNhan(NguoiDung nguoiNhan) { this.nguoiNhan = nguoiNhan; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDateTime getGuiLuc() { return guiLuc; }
    public void setGuiLuc(LocalDateTime guiLuc) { this.guiLuc = guiLuc; }
    public Boolean getDaDoc() { return daDoc; }
    public void setDaDoc(Boolean daDoc) { this.daDoc = daDoc; }
}
