package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.DatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.repository.projection.StaffAggregationRow;
import com.team6.quanlyvemaybay.repository.projection.TimeAggregationRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository truy vấn thống kê và báo cáo tổng hợp trên bảng dat_cho.
 */
public interface DatChoRepository extends JpaRepository<DatCho, Long> {

    @Query("""
            SELECT nv.nguoiDungId AS nhanVienId,
                   COALESCE(nv.maNhanVien, 'ONLINE') AS maNhanVien,
                   COALESCE(nd.hoTen, 'Khách tự đặt Online') AS hoTenNhanVien,
                   COUNT(dc.id) AS soDonDatCho,
                   SUM(dc.tongGiaGoc) AS tongGiaGoc,
                   SUM(dc.tongGiaBan) AS doanhThu
            FROM DatCho dc
            LEFT JOIN dc.nhanVien nv
            LEFT JOIN nv.nguoiDung nd
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThai
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR EXISTS (
                  SELECT 1 FROM VeMayBay vf
                  WHERE vf.datCho = dc
                    AND vf.hangVe.chuyenBay.hangHangKhong.maHang = :maHang
              ))
            GROUP BY nv.nguoiDungId, nv.maNhanVien, nd.hoTen
            ORDER BY SUM(dc.tongGiaBan) DESC
            """)
    List<StaffAggregationRow> aggregateByStaff(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThai") TrangThaiDatCho trangThai,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   MONTH(dc.ngayDat) AS thang,
                   DAY(dc.ngayDat) AS ngay,
                   COUNT(dc.id) AS soDonDatCho,
                   SUM(dc.tongGiaGoc) AS tongGiaGoc,
                   SUM(dc.tongGiaBan) AS doanhThu
            FROM DatCho dc
            LEFT JOIN dc.nhanVien nv
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThai
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR EXISTS (
                  SELECT 1 FROM VeMayBay vf
                  WHERE vf.datCho = dc
                    AND vf.hangVe.chuyenBay.hangHangKhong.maHang = :maHang
              ))
            GROUP BY YEAR(dc.ngayDat), MONTH(dc.ngayDat), DAY(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC, MONTH(dc.ngayDat) ASC, DAY(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateByDay(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThai") TrangThaiDatCho trangThai,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   MONTH(dc.ngayDat) AS thang,
                   COUNT(dc.id) AS soDonDatCho,
                   SUM(dc.tongGiaGoc) AS tongGiaGoc,
                   SUM(dc.tongGiaBan) AS doanhThu
            FROM DatCho dc
            LEFT JOIN dc.nhanVien nv
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThai
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR EXISTS (
                  SELECT 1 FROM VeMayBay vf
                  WHERE vf.datCho = dc
                    AND vf.hangVe.chuyenBay.hangHangKhong.maHang = :maHang
              ))
            GROUP BY YEAR(dc.ngayDat), MONTH(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC, MONTH(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateByMonth(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThai") TrangThaiDatCho trangThai,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );

    @Query("""
            SELECT YEAR(dc.ngayDat) AS nam,
                   COUNT(dc.id) AS soDonDatCho,
                   SUM(dc.tongGiaGoc) AS tongGiaGoc,
                   SUM(dc.tongGiaBan) AS doanhThu
            FROM DatCho dc
            LEFT JOIN dc.nhanVien nv
            WHERE dc.ngayDat >= :startDateTime
              AND dc.ngayDat < :endExclusive
              AND dc.trangThai = :trangThai
              AND (:nhanVienId IS NULL OR nv.nguoiDungId = :nhanVienId)
              AND (:maHang IS NULL OR EXISTS (
                  SELECT 1 FROM VeMayBay vf
                  WHERE vf.datCho = dc
                    AND vf.hangVe.chuyenBay.hangHangKhong.maHang = :maHang
              ))
            GROUP BY YEAR(dc.ngayDat)
            ORDER BY YEAR(dc.ngayDat) ASC
            """)
    List<TimeAggregationRow> aggregateByYear(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endExclusive") LocalDateTime endExclusive,
            @Param("trangThai") TrangThaiDatCho trangThai,
            @Param("nhanVienId") Long nhanVienId,
            @Param("maHang") String maHang
    );
}
