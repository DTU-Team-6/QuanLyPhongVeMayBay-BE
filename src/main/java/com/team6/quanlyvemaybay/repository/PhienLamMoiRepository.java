package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.PhienLamMoi;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhienLamMoiRepository extends JpaRepository<PhienLamMoi, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PhienLamMoi p where p.tokenHash = :hash")
    Optional<PhienLamMoi> findByTokenHashForUpdate(@Param("hash") String hash);

    @Modifying
    @Query("update PhienLamMoi p set p.daThuHoi = true where p.nhomPhien = :nhom")
    int thuHoiNhom(@Param("nhom") String nhom);

    @Modifying
    @Query("update PhienLamMoi p set p.daThuHoi = true where p.nguoiDungId = :nguoiDungId")
    int thuHoiNguoiDung(@Param("nguoiDungId") Long nguoiDungId);
}
