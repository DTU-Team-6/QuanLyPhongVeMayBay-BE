package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.SuaNhanVienResponse;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.NhanVien;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.NhanVienRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public NhanVienService(NhanVienRepository nhanVienRepository, NguoiDungRepository nguoiDungRepository) {
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Transactional
    public SuaNhanVienResponse suaNhanVien(Long id, SuaNhanVienRequest request) {
        NhanVien nhanVien = nhanVienRepository.findWithNguoiDungById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "Không tìm thấy nhân viên."));
        if (request.vaiTro() != VaiTro.ADMIN && request.vaiTro() != VaiTro.NHAN_VIEN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Vai trò nhân viên không hợp lệ.",
                    List.of(new ErrorDetail("vaiTro", "INVALID_FIELD", "Chỉ nhận ADMIN hoặc NHAN_VIEN")));
        }
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCaseAndIdNot(email, id)) {
            throw trungEmail();
        }
        NguoiDung nguoiDung = nhanVien.getNguoiDung();
        nguoiDung.setHoTen(request.hoTen().strip());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setTaiKhoanEmail(email);
        nguoiDung.setSoDienThoai(request.soDienThoai());
        nguoiDung.setVaiTro(request.vaiTro());
        try {
            nguoiDungRepository.saveAndFlush(nguoiDung);
        } catch (DataIntegrityViolationException ex) {
            // UNIQUE trong DB vẫn chặn email trùng nếu có yêu cầu đồng thời.
            throw trungEmail();
        }
        return new SuaNhanVienResponse(id, nhanVien.getMaNhanVien(), nguoiDung.getHoTen(),
                nguoiDung.getNgaySinh(), nguoiDung.getGioiTinh(), email, nguoiDung.getSoDienThoai(),
                nguoiDung.getVaiTro(), nguoiDung.getTrangThai());
    }

    private BusinessException trungEmail() {
        return new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_EMPLOYEE_EMAIL", "Email đã được sử dụng.",
                List.of(new ErrorDetail("email", "DUPLICATE", "Email đã được sử dụng.")));
    }
}
