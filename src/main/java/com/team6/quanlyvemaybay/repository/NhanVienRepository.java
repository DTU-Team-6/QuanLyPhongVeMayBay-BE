package com.team6.quanlyvemaybay.repository;

import com.team6.quanlyvemaybay.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    @Query(value = """
            select nv from NhanVien nv join fetch nv.nguoiDung nd
            where nd.trangThai = 1 and (:tuKhoa = '' or
                (:tieuChi = 'MA_NHAN_VIEN' and locate(lower(:tuKhoa), lower(nv.maNhanVien)) > 0) or
                (:tieuChi = 'HO_TEN' and locate(lower(:tuKhoa), lower(nd.hoTen)) > 0) or
                (:tieuChi = 'EMAIL' and locate(lower(:tuKhoa), lower(nd.taiKhoanEmail)) > 0) or
                (:tieuChi = 'SO_DIEN_THOAI' and locate(lower(:tuKhoa), lower(nd.soDienThoai)) > 0))
            order by nv.nguoiDungId asc
            """, countQuery = """
            select count(nv) from NhanVien nv join nv.nguoiDung nd
            where nd.trangThai = 1 and (:tuKhoa = '' or
                (:tieuChi = 'MA_NHAN_VIEN' and locate(lower(:tuKhoa), lower(nv.maNhanVien)) > 0) or
                (:tieuChi = 'HO_TEN' and locate(lower(:tuKhoa), lower(nd.hoTen)) > 0) or
                (:tieuChi = 'EMAIL' and locate(lower(:tuKhoa), lower(nd.taiKhoanEmail)) > 0) or
                (:tieuChi = 'SO_DIEN_THOAI' and locate(lower(:tuKhoa), lower(nd.soDienThoai)) > 0))
            """)
    Page<NhanVien> timNhanVien(@Param("tieuChi") String tieuChi, @Param("tuKhoa") String tuKhoa,
                              Pageable pageable);
}
