package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.nhanvien.DanhSachNhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.NhanVienResponse;
import com.team6.quanlyvemaybay.dto.nhanvien.TieuChiTimNhanVien;
import com.team6.quanlyvemaybay.entity.NhanVien;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NhanVienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NhanVienService {
    private final NhanVienRepository repository;

    public NhanVienService(NhanVienRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DanhSachNhanVienResponse danhSach(TieuChiTimNhanVien tieuChi, String tuKhoa, int trang, int kichThuoc) {
        String keyword = tuKhoa == null ? "" : tuKhoa.strip();
        if ((tieuChi == null) != keyword.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_SEARCH",
                    "Cần cung cấp đồng thời tiêu chí và từ khóa tìm kiếm.");
        }
        Page<NhanVien> page = repository.timNhanVien(tieuChi == null ? "" : tieuChi.name(), keyword,
                PageRequest.of(trang, kichThuoc));
        return new DanhSachNhanVienResponse(page.getContent().stream().map(this::chuyenDoi).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private NhanVienResponse chuyenDoi(NhanVien nv) {
        NguoiDung nd = nv.getNguoiDung();
        return new NhanVienResponse(nd.getId(), nv.getMaNhanVien(), nd.getHoTen(), nd.getNgaySinh(),
                nd.getGioiTinh(), nd.getTaiKhoanEmail(), nd.getSoDienThoai(), nd.getVaiTro(), nd.getTrangThai());
    }
}
