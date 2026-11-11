package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.TinTuc;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Repository truy vấn tin tức, khuyến mãi và thông báo chung.
 */
public interface TinTucRepository extends JpaRepository<TinTuc, Long> {

    List<TinTuc> findByTrangThaiAndLoaiTinInOrderByThoiGianTaoDesc(
            TrangThaiTinTuc trangThai,
            Collection<LoaiTinTuc> loaiTins,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM TinTuc t
            WHERE (:keyword IS NULL OR LOWER(t.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                   OR LOWER(t.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:loaiTin IS NULL OR t.loaiTin = :loaiTin)
              AND (:trangThai IS NULL OR t.trangThai = :trangThai)
            ORDER BY t.thoiGianTao DESC
            """)
    Page<TinTuc> findWithFilters(
            @Param("keyword") String keyword,
            @Param("loaiTin") LoaiTinTuc loaiTin,
            @Param("trangThai") TrangThaiTinTuc trangThai,
            Pageable pageable
    );
}
