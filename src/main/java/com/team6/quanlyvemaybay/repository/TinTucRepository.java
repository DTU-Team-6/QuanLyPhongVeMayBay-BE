package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.TinTuc;
import com.team6.quanlyvemaybay.entity.enums.LoaiTinTuc;
import com.team6.quanlyvemaybay.entity.enums.TrangThaiTinTuc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Repository truy vấn tin tức, khuyến mãi và thông báo chung.
 */
public interface TinTucRepository extends JpaRepository<TinTuc, Long> {

    /**
     * Lấy các bài viết đang hiển thị theo danh sách phân loại, sắp xếp mới nhất trước.
     */
    List<TinTuc> findByTrangThaiAndLoaiTinInOrderByThoiGianTaoDesc(
            TrangThaiTinTuc trangThai,
            Collection<LoaiTinTuc> loaiTins,
            Pageable pageable
    );
}
