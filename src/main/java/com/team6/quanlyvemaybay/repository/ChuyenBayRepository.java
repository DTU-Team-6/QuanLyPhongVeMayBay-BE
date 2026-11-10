package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.ChuyenBay;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý lịch trình chuyến bay.
 */
public interface ChuyenBayRepository extends JpaRepository<ChuyenBay, Long> {
}
