package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.HangVeChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiChuyenBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiHang;
import com.team6.quanlyvemaybay.repository.projection.FlightOccupancyRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository truy vấn hạng vé, giá bán và thống kê công suất ghế theo từng chuyến bay.
 */
public interface HangVeChuyenBayRepository extends JpaRepository<HangVeChuyenBay, Long> {

    @Query("""
            SELECT hv FROM HangVeChuyenBay hv
            JOIN FETCH hv.chuyenBay cb
            JOIN FETCH cb.hangHangKhong h
            JOIN FETCH cb.sanBayDi di
            JOIN FETCH cb.sanBayDen den
            WHERE di.maSanBay = :maDi AND den.maSanBay = :maDen
              AND cb.thoiGianKhoiHanh >= :tu AND cb.thoiGianKhoiHanh < :denGio
              AND cb.trangThai = :trangThai AND h.trangThai = :trangThaiHang
              AND hv.soGheTrong >= :soCho
              AND (:maHang IS NULL OR h.maHang = :maHang)
            ORDER BY cb.thoiGianKhoiHanh ASC, hv.giaBan ASC
            """)
    List<HangVeChuyenBay> findForSearch(
            @Param("maDi") String maDi, @Param("maDen") String maDen,
            @Param("tu") LocalDateTime tu, @Param("denGio") LocalDateTime denGio,
            @Param("trangThai") TrangThaiChuyenBay trangThai,
            @Param("trangThaiHang") TrangThaiHang trangThaiHang,
            @Param("soCho") int soCho, @Param("maHang") String maHang);

    @Query("""
            SELECT hv FROM HangVeChuyenBay hv
            JOIN FETCH hv.chuyenBay cb
            JOIN FETCH cb.hangHangKhong h
            JOIN FETCH cb.sanBayDi
            JOIN FETCH cb.sanBayDen
            WHERE cb.id = :id
            """)
    List<HangVeChuyenBay> findByFlightIdWithDetails(@Param("id") Long id);

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

    @Query("""
            SELECT YEAR(cb.thoiGianKhoiHanh) AS nam,
                   MONTH(cb.thoiGianKhoiHanh) AS thang,
                   DAY(cb.thoiGianKhoiHanh) AS ngay,
                   COUNT(DISTINCT cb.id) AS soChuyenBay,
                   SUM(hv.tongSoGhe) AS tongSoGhe,
                   SUM(hv.soGheTrong) AS soGheTrong
            FROM HangVeChuyenBay hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE cb.thoiGianKhoiHanh >= :startDateTime
              AND cb.thoiGianKhoiHanh < :endExclusive
              AND cb.trangThai <> :trangThaiHuy
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(cb.thoiGianKhoiHanh), MONTH(cb.thoiGianKhoiHanh), DAY(cb.thoiGianKhoiHanh)
            ORDER BY YEAR(cb.thoiGianKhoiHanh) ASC, MONTH(cb.thoiGianKhoiHanh) ASC, DAY(cb.thoiGianKhoiHanh) ASC
            """)
    List<FlightOccupancyRow> aggregateOccupancyByDay(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiHuy") TrangThaiChuyenBay trangThaiHuy,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(cb.thoiGianKhoiHanh) AS nam,
                   MONTH(cb.thoiGianKhoiHanh) AS thang,
                   COUNT(DISTINCT cb.id) AS soChuyenBay,
                   SUM(hv.tongSoGhe) AS tongSoGhe,
                   SUM(hv.soGheTrong) AS soGheTrong
            FROM HangVeChuyenBay hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE cb.thoiGianKhoiHanh >= :startDateTime
              AND cb.thoiGianKhoiHanh < :endExclusive
              AND cb.trangThai <> :trangThaiHuy
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(cb.thoiGianKhoiHanh), MONTH(cb.thoiGianKhoiHanh)
            ORDER BY YEAR(cb.thoiGianKhoiHanh) ASC, MONTH(cb.thoiGianKhoiHanh) ASC
            """)
    List<FlightOccupancyRow> aggregateOccupancyByMonth(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiHuy") TrangThaiChuyenBay trangThaiHuy,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(cb.thoiGianKhoiHanh) AS nam,
                   COUNT(DISTINCT cb.id) AS soChuyenBay,
                   SUM(hv.tongSoGhe) AS tongSoGhe,
                   SUM(hv.soGheTrong) AS soGheTrong
            FROM HangVeChuyenBay hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE cb.thoiGianKhoiHanh >= :startDateTime
              AND cb.thoiGianKhoiHanh < :endExclusive
              AND cb.trangThai <> :trangThaiHuy
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(cb.thoiGianKhoiHanh)
            ORDER BY YEAR(cb.thoiGianKhoiHanh) ASC
            """)
    List<FlightOccupancyRow> aggregateOccupancyByYear(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiHuy") TrangThaiChuyenBay trangThaiHuy,
            @Param("maHang") String maHang
    );
}
