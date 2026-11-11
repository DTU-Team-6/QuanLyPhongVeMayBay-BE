package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    boolean existsByMaNhanVienIgnoreCase(String maNhanVien);
}
