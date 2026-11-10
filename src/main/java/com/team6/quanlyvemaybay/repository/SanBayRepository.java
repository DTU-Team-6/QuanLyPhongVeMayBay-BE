package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.SanBay;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository quản lý danh mục sân bay.
 */
public interface SanBayRepository extends JpaRepository<SanBay, String> {
}
