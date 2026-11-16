package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.SanBay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Repository quản lý danh mục sân bay. */
public interface SanBayRepository extends JpaRepository<SanBay, String> {

    /** Khoá sân bay trước khi kiểm tra trùng để tuần tự hoá thao tác thêm điểm đến cùng sân bay. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sb FROM SanBay sb WHERE sb.maSanBay = :maSanBay")
    Optional<SanBay> findByIdForUpdate(@Param("maSanBay") String maSanBay);
}
