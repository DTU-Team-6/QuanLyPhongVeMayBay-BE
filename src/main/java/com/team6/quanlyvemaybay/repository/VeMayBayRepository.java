package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.VeMayBay;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiDatCho;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiVe;
import com.team6.quanlyvemaybay.repository.projection.AirlineAggregationRow;
import com.team6.quanlyvemaybay.repository.projection.StaffTicketCountRow;
import com.team6.quanlyvemaybay.repository.projection.TimeAggregationRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository truy vấn thống kê vé bán ra và doanh thu chi tiết trên bảng ve_may_bay.
 */
public interface VeMayBayRepository extends JpaRepository<VeMayBay, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VeMayBay v join fetch v.datCho where v.id = :id")
    Optional<VeMayBay> findForTicketUpdate(@Param("id") Long id);

    List<VeMayBay> findByDatChoId(Long datChoId);

    @Query(value = """
            select v from VeMayBay v
            join fetch v.datCho dc
            join fetch v.hangVe hv
            join fetch hv.chuyenBay cb
            join fetch cb.sanBayDi
            join fetch cb.sanBayDen
            where (:trangThai is null or v.trangThai = :trangThai)
              and (:tuKhoa = '' or
                (:tieuChi = 'MA_VE' and locate(lower(:tuKhoa), lower(v.maVe)) > 0) or
                (:tieuChi = 'MA_DAT_CHO' and locate(lower(:tuKhoa), lower(dc.maDatCho)) > 0) or
                (:tieuChi = 'TEN_NGUOI_DI' and locate(lower(:tuKhoa), lower(v.hoTenNguoiDi)) > 0) or
                (:tieuChi = 'MA_CHUYEN_BAY' and locate(lower(:tuKhoa), lower(cb.maChuyenBay)) > 0))
            order by v.id desc
            """, countQuery = """
            select count(v) from VeMayBay v
            join v.datCho dc join v.hangVe hv join hv.chuyenBay cb
            where (:trangThai is null or v.trangThai = :trangThai)
              and (:tuKhoa = '' or
                (:tieuChi = 'MA_VE' and locate(lower(:tuKhoa), lower(v.maVe)) > 0) or
                (:tieuChi = 'MA_DAT_CHO' and locate(lower(:tuKhoa), lower(dc.maDatCho)) > 0) or
                (:tieuChi = 'TEN_NGUOI_DI' and locate(lower(:tuKhoa), lower(v.hoTenNguoiDi)) > 0) or
                (:tieuChi = 'MA_CHUYEN_BAY' and locate(lower(:tuKhoa), lower(cb.maChuyenBay)) > 0))
            """)
    Page<VeMayBay> timDanhSachVe(@Param("trangThai") TrangThaiVe trangThai,
                                 @Param("tieuChi") String tieuChi,
                                 @Param("tuKhoa") String tuKhoa,
                                 Pageable pageable);

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
