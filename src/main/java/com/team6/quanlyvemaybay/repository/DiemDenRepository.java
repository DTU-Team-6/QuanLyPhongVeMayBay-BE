package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.DiemDen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy vấn và quản lý bảng diem_den.
 */
public interface DiemDenRepository extends JpaRepository<DiemDen, Long> {

    /**
     * Tìm kiếm điểm đến theo từ khóa (tên điểm đến, mã/tên sân bay, thành phố) hoặc lọc theo mã sân bay, có phân trang.
     */
    @Query(
            value = """
                    SELECT dd
                    FROM DiemDen dd
                    JOIN FETCH dd.sanBay sb
                    WHERE (:maSanBay IS NULL OR UPPER(sb.maSanBay) = :maSanBay)
                      AND (:keyword IS NULL
                           OR LOWER(dd.tenDiemDen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.maSanBay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.tenSanBay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.thanhPho) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    ORDER BY dd.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(dd)
                    FROM DiemDen dd
                    JOIN dd.sanBay sb
                    WHERE (:maSanBay IS NULL OR UPPER(sb.maSanBay) = :maSanBay)
                      AND (:keyword IS NULL
                           OR LOWER(dd.tenDiemDen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.maSanBay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.tenSanBay) LIKE LOWER(CONCAT('%', :keyword, '%'))
                           OR LOWER(sb.thanhPho) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    """
    )
    Page<DiemDen> findWithFilters(
            @Param("keyword") String keyword,
            @Param("maSanBay") String maSanBay,
            Pageable pageable
    );

    /**
     * Kiểm tra trùng tên điểm đến trong cùng một sân bay (không phân biệt hoa thường và khoảng trắng thừa).
     */
    @Query("""
            SELECT COUNT(dd) > 0
            FROM DiemDen dd
            WHERE LOWER(TRIM(dd.tenDiemDen)) = LOWER(:tenDiemDen)
              AND UPPER(dd.sanBay.maSanBay) = UPPER(:maSanBay)
            """)
    boolean existsDuplicateInAirport(
            @Param("tenDiemDen") String tenDiemDen,
            @Param("maSanBay") String maSanBay
    );
}
