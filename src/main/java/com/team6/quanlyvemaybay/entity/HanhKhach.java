package com.team6.quanlyvemaybay.entity;

import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "hanh_khach")
public class HanhKhach {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="dat_cho_id", nullable=false) private Long datChoId;
    @Column(name="ho_ten", nullable=false) private String hoTen;
    @Column(name="ngay_sinh", nullable=false) private LocalDate ngaySinh;
    @Enumerated(EnumType.STRING) @Column(name="gioi_tinh", nullable=false) private GioiTinh gioiTinh = GioiTinh.Nu;
    private String email;
    @Column(name="so_dien_thoai") private String soDienThoai;
    @Column(name="cmnd_passport") private String cmndPassport;
    @Column(name="loai_hanh_khach", nullable=false) private String loaiHanhKhach = "NGUOI_LON";
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getDatChoId(){return datChoId;} public void setDatChoId(Long v){datChoId=v;}
    public String getHoTen(){return hoTen;} public void setHoTen(String v){hoTen=v;}
    public LocalDate getNgaySinh(){return ngaySinh;} public void setNgaySinh(LocalDate v){ngaySinh=v;}
    public GioiTinh getGioiTinh(){return gioiTinh;} public void setGioiTinh(GioiTinh v){gioiTinh=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getSoDienThoai(){return soDienThoai;} public void setSoDienThoai(String v){soDienThoai=v;}
    public String getCmndPassport(){return cmndPassport;} public void setCmndPassport(String v){cmndPassport=v;}
    public String getLoaiHanhKhach(){return loaiHanhKhach;} public void setLoaiHanhKhach(String v){loaiHanhKhach=v;}
}
