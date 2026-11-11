package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByTaiKhoanEmail(String taiKhoanEmail);
    boolean existsByTaiKhoanEmailIgnoreCaseAndIdNot(String taiKhoanEmail, Long id);
}
