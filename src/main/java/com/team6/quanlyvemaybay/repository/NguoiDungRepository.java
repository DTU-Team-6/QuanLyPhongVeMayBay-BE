package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByTaiKhoanEmail(String taiKhoanEmail);
    Optional<NguoiDung> findByTaiKhoanEmailIgnoreCase(String taiKhoanEmail);
    boolean existsByTaiKhoanEmailIgnoreCase(String taiKhoanEmail);
    boolean existsByTaiKhoanEmailIgnoreCaseAndIdNot(String taiKhoanEmail, Long id);

    @Modifying
    @Query("update NguoiDung nd set nd.trangThai = 0 where nd.id = :id and nd.trangThai = 1")
    int ngungHoatDongNeuDangHoatDong(@Param("id") Long id);
}
