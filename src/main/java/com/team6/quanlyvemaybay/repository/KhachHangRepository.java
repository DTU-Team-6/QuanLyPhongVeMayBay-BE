package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {

    @Query(
            value = """
                    SELECT kh
                    FROM KhachHang kh
                    JOIN FETCH kh.nguoiDung nd
                    WHERE (:keyword IS NULL
                           OR LOWER(nd.taiKhoanEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nd.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(kh.cmndPassport) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    ORDER BY kh.nguoiDungId DESC
                    """,
            countQuery = """
                    SELECT COUNT(kh)
                    FROM KhachHang kh
                    JOIN kh.nguoiDung nd
                    WHERE (:keyword IS NULL
                           OR LOWER(nd.taiKhoanEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nd.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(nd.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(kh.cmndPassport) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    """
    )
    Page<KhachHang> findWithFilters(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT kh
            FROM KhachHang kh
            JOIN FETCH kh.nguoiDung
            WHERE kh.nguoiDungId = :id
            """)
    Optional<KhachHang> findWithNguoiDungByNguoiDungId(@Param("id") Long id);
}
