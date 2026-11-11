package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.VeMayBay;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.repository.projection.AirlineAggregationRow;
import com.team6.quanlyvemaybay.repository.projection.StaffTicketCountRow;
import com.team6.quanlyvemaybay.repository.projection.TimeAggregationRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository truy vấn thống kê vé bán ra và doanh thu chi tiết trên bảng ve_may_bay.
 */
public interface VeMayBayRepository extends JpaRepository<VeMayBay, Long> {

    List<VeMayBay> findByDatChoId(Long datChoId);

    @Query("""
            SELECT hhk.maHang AS maHang,
                   hhk.tenHang AS tenHang,
                   COUNT(DISTINCT dc.id) AS soDonDatCho,
                   COUNT(v.id) AS soVeBanRa,
                   SUM(v.giaGoc) AS tongGiaGoc,
                   SUM(v.tongGia) AS doanhThu
            FROM VeMayBay v
            JOIN v.datCho dc
            LEFT JOIN dc.nhanVien nv
            JOIN v.hangVe hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThaiDatCho
              AND v.trangThai = :trangThaiVe
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
            GROUP BY hhk.maHang, hhk.tenHang
            ORDER BY SUM(v.tongGia) DESC
            """)
    List<AirlineAggregationRow> aggregateByAirline(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiDatCho") TrangThaiDatCho trangThaiDatCho,
            @Param("trangThaiVe") TrangThaiVe trangThaiVe,
            @Param("maHang") String maHang,
            @Param("nhanVienId") Long nhanVienId
    );

    @Query("""
            SELECT nv.nguoiDungId AS nhanVienId,
                   COUNT(v.id) AS soVe
            FROM VeMayBay v
            JOIN v.datCho dc
            LEFT JOIN dc.nhanVien nv
            JOIN v.hangVe hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThaiDatCho
              AND v.trangThai = :trangThaiVe
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY nv.nguoiDungId
            """)
    List<StaffTicketCountRow> countTicketsByStaff(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiDatCho") TrangThaiDatCho trangThaiDatCho,
            @Param("trangThaiVe") TrangThaiVe trangThaiVe,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   MONTH(dc.ngayDat) AS thang,
                   DAY(dc.ngayDat) AS ngay,
                   COUNT(DISTINCT dc.id) AS soDonDatCho,
                   COUNT(v.id) AS soVeBanRa,
                   SUM(v.giaGoc) AS tongGiaGoc,
                   SUM(v.tongGia) AS doanhThu
            FROM VeMayBay v
            JOIN v.datCho dc
            LEFT JOIN dc.nhanVien nv
            JOIN v.hangVe hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThaiDatCho
              AND v.trangThai = :trangThaiVe
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(dc.ngayDat), MONTH(dc.ngayDat), DAY(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC, MONTH(dc.ngayDat) ASC, DAY(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateTicketsByDay(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiDatCho") TrangThaiDatCho trangThaiDatCho,
            @Param("trangThaiVe") TrangThaiVe trangThaiVe,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   MONTH(dc.ngayDat) AS thang,
                   COUNT(DISTINCT dc.id) AS soDonDatCho,
                   COUNT(v.id) AS soVeBanRa,
                   SUM(v.giaGoc) AS tongGiaGoc,
                   SUM(v.tongGia) AS doanhThu
            FROM VeMayBay v
            JOIN v.datCho dc
            LEFT JOIN dc.nhanVien nv
            JOIN v.hangVe hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThaiDatCho
              AND v.trangThai = :trangThaiVe
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(dc.ngayDat), MONTH(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC, MONTH(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateTicketsByMonth(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiDatCho") TrangThaiDatCho trangThaiDatCho,
            @Param("trangThaiVe") TrangThaiVe trangThaiVe,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   COUNT(DISTINCT dc.id) AS soDonDatCho,
                   COUNT(v.id) AS soVeBanRa,
                   SUM(v.giaGoc) AS tongGiaGoc,
                   SUM(v.tongGia) AS doanhThu
            FROM VeMayBay v
            JOIN v.datCho dc
            LEFT JOIN dc.nhanVien nv
            JOIN v.hangVe hv
            JOIN hv.chuyenBay cb
            JOIN cb.hangHangKhong hhk
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThaiDatCho
              AND v.trangThai = :trangThaiVe
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR hhk.maHang = :maHang)
            GROUP BY YEAR(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateTicketsByYear(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThaiDatCho") TrangThaiDatCho trangThaiDatCho,
            @Param("trangThaiVe") TrangThaiVe trangThaiVe,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );
}
