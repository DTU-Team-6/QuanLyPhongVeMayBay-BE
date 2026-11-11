package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienRequest;
import com.team6.quanlyvemaybay.dto.nhanvien.ThemNhanVienResponse;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.NhanVien;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.NhanVienRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class NhanVienService {
    private final NguoiDungRepository nguoiDungRepository;
    private final NhanVienRepository nhanVienRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public NhanVienService(NguoiDungRepository nguoiDungRepository, NhanVienRepository nhanVienRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @Transactional
    public ThemNhanVienResponse themNhanVien(ThemNhanVienRequest request) {
        String maNhanVien = request.maNhanVien().strip().toUpperCase(Locale.ROOT);
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (request.vaiTro() != VaiTro.ADMIN && request.vaiTro() != VaiTro.NHAN_VIEN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Vai trò nhân viên không hợp lệ.",
                    List.of(new ErrorDetail("vaiTro", "INVALID_FIELD", "Chỉ nhận ADMIN hoặc NHAN_VIEN")));
        }
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase(email)) {
            throw trungDuLieu("email", "Email đã được sử dụng.");
        }
        if (nhanVienRepository.existsByMaNhanVienIgnoreCase(maNhanVien)) {
            throw trungDuLieu("maNhanVien", "Mã nhân viên đã tồn tại.");
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setTaiKhoanEmail(email);
        nguoiDung.setMatKhau(passwordEncoder.encode(request.matKhau()));
        nguoiDung.setHoTen(request.hoTen().strip());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setSoDienThoai(request.soDienThoai());
        nguoiDung.setVaiTro(request.vaiTro());
        nguoiDung.setTrangThai((byte) 1);

        NhanVien nhanVien = new NhanVien();
        nhanVien.setNguoiDung(nguoiDung);
        nhanVien.setMaNhanVien(maNhanVien);
        try {
            nguoiDungRepository.saveAndFlush(nguoiDung);
            nhanVienRepository.saveAndFlush(nhanVien);
        } catch (DataIntegrityViolationException ex) {
            // Ràng buộc UNIQUE trong DB bảo vệ cả khi hai yêu cầu đồng thời vượt qua bước kiểm tra trước.
            throw new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_EMPLOYEE",
                    "Email hoặc mã nhân viên đã tồn tại.");
        }
        return new ThemNhanVienResponse(nguoiDung.getId(), maNhanVien, nguoiDung.getHoTen(),
                nguoiDung.getNgaySinh(), nguoiDung.getGioiTinh(), email, nguoiDung.getSoDienThoai(),
                nguoiDung.getVaiTro(), nguoiDung.getTrangThai());
    }

    private BusinessException trungDuLieu(String field, String message) {
        return new BusinessException(HttpStatus.CONFLICT, "DUPLICATE_EMPLOYEE", message,
                List.of(new ErrorDetail(field, "DUPLICATE", message)));
    }
}
