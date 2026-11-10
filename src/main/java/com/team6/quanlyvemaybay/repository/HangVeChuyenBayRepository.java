package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository truy vấn hạng vé và giá bán theo từng chuyến bay.
 */
public interface HangVeChuyenBayRepository extends JpaRepository<HangVeChuyenBay, Long> {

    /**
     * Lấy danh sách hạng vé rẻ nhất của các chuyến bay còn khả dụng và chưa cất cánh,
     * sắp xếp tăng dần theo tổng giá vé (giá bán + thuế phí).
     */
    @Query("""
            SELECT hv
            FROM HangVeChuyenBay hv
            JOIN FETCH hv.chuyenBay cb
            JOIN FETCH cb.hangHangKhong hhk
            JOIN FETCH cb.sanBayDi sbDi
            JOIN FETCH cb.sanBayDen sbDen
            WHERE cb.trangThai <> :trangThaiHuy
              AND hhk.trangThai = :trangThaiHangHoatDong
              AND cb.thoiGianKhoiHanh > :thoiDiemHienTai
              AND hv.soGheTrong > 0
              AND (hv.giaBan + hv.thuePhi) = (
                  SELECT MIN(hv2.giaBan + hv2.thuePhi)
                  FROM HangVeChuyenBay hv2
                  WHERE hv2.chuyenBay.id = cb.id
                    AND hv2.soGheTrong > 0
              )
            ORDER BY (hv.giaBan + hv.thuePhi) ASC, hv.giaBan ASC, cb.thoiGianKhoiHanh ASC, hv.id ASC
            """)
    List<HangVeChuyenBay> findTopCheapestAvailableFlights(
            @Param("trangThaiHuy") TrangThaiChuyenBay trangThaiHuy,
            @Param("trangThaiHangHoatDong") TrangThaiHang trangThaiHangHoatDong,
            @Param("thoiDiemHienTai") LocalDateTime thoiDiemHienTai,
            Pageable pageable
    );
}
