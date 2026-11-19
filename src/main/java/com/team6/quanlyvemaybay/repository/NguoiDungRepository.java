package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByTaiKhoanEmail(String taiKhoanEmail);
    Optional<NguoiDung> findByTaiKhoanEmailIgnoreCase(String taiKhoanEmail);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NguoiDung n where lower(n.taiKhoanEmail) = lower(:email)")
    Optional<NguoiDung> findByEmailForUpdate(@Param("email") String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NguoiDung n where n.id = :id")
    Optional<NguoiDung> findByIdForUpdate(@Param("id") Long id);
    Optional<NguoiDung> findFirstByVaiTroAndTrangThaiOrderByIdAsc(VaiTro vaiTro, Byte trangThai);
    boolean existsByTaiKhoanEmailIgnoreCase(String taiKhoanEmail);
    boolean existsByTaiKhoanEmailIgnoreCaseAndIdNot(String taiKhoanEmail, Long id);

    @Modifying
    @Query("update NguoiDung nd set nd.trangThai = 0 where nd.id = :id and nd.trangThai = 1")
    int ngungHoatDongNeuDangHoatDong(@Param("id") Long id);
}
