package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long>, JpaSpecificationExecutor<ThanhToan> {

    Optional<ThanhToan> findByMaGiaoDich(String maGiaoDich);

    Optional<ThanhToan> findByMaGiaoDichDoiTac(String maGiaoDichDoiTac);

    List<ThanhToan> findByDatChoIdOrderByThoiGianThanhToanDesc(Long datChoId);
}
