package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    @Query("select nv from NhanVien nv join fetch nv.nguoiDung where nv.nguoiDungId = :id")
    Optional<NhanVien> findWithNguoiDungById(@Param("id") Long id);
}
