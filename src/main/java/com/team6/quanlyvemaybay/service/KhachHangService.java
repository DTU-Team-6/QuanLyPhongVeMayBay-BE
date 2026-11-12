package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.common.PageResponse;
import com.team6.quanlyvemaybay.dto.khachhang.CapNhatKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.KhachHangResponse;
import com.team6.quanlyvemaybay.dto.khachhang.TaoKhachHangRequest;
import com.team6.quanlyvemaybay.dto.khachhang.XoaKhachHangResponse;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public KhachHangService(
            KhachHangRepository khachHangRepository,
            NguoiDungRepository nguoiDungRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.khachHangRepository = khachHangRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<KhachHangResponse> layDanhSach(String keyword, int page, int size) {
        String normalizedKeyword = normalizeOptionalText(keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<KhachHangResponse> result = khachHangRepository.findWithFilters(normalizedKeyword, pageable)
                .map(this::toResponse);
        return PageResponse.of(result);
    }

    @Transactional
    public KhachHangResponse themMoi(TaoKhachHangRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase(email)) {
            throw emailDaTonTai();
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setTaiKhoanEmail(email);
        nguoiDung.setMatKhau(encodeOptionalPassword(request.matKhau()));
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(normalizeOptionalText(request.soDienThoai()));
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh() == null ? GioiTinh.Khac : request.gioiTinh());
        nguoiDung.setDiaChi(normalizeOptionalText(request.diaChi()));
        nguoiDung.setVaiTro(VaiTro.KHACH_HANG);
        nguoiDung.setTrangThai((byte) 1);

        try {
            nguoiDung = nguoiDungRepository.saveAndFlush(nguoiDung);
        } catch (DataIntegrityViolationException ex) {
            throw emailDaTonTai();
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setNguoiDung(nguoiDung);
        khachHang.setCmndPassport(normalizeOptionalText(request.cmndPassport()));
        khachHang.setQuocTich(defaultIfNull(normalizeOptionalText(request.quocTich()), "Việt Nam"));
        khachHang.setXepHang(defaultIfNull(request.xepHang(), "Member"));
        khachHang.setDiemTichLuy(request.diemTichLuy() == null ? 0 : request.diemTichLuy());
        return toResponse(khachHangRepository.save(khachHang));
    }

    @Transactional
    public KhachHangResponse capNhat(Long id, CapNhatKhachHangRequest request) {
        KhachHang khachHang = timKhachHangDangHoatDong(id);
        NguoiDung nguoiDung = khachHang.getNguoiDung();
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCaseAndIdNot(email, id)) {
            throw emailDaTonTai();
        }

        nguoiDung.setTaiKhoanEmail(email);
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(normalizeOptionalText(request.soDienThoai()));
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setDiaChi(normalizeOptionalText(request.diaChi()));
        khachHang.setCmndPassport(normalizeOptionalText(request.cmndPassport()));
        khachHang.setQuocTich(request.quocTich().trim());
        khachHang.setXepHang(request.xepHang());
        khachHang.setDiemTichLuy(request.diemTichLuy());

        try {
            nguoiDungRepository.saveAndFlush(nguoiDung);
        } catch (DataIntegrityViolationException ex) {
            throw emailDaTonTai();
        }
        return toResponse(khachHangRepository.save(khachHang));
    }

    @Transactional
    public XoaKhachHangResponse ngungHoatDong(Long id) {
        timKhachHangDangHoatDong(id);
        if (nguoiDungRepository.ngungHoatDongNeuDangHoatDong(id) != 1) {
            throw khachHangDaNgungHoatDong();
        }
        return new XoaKhachHangResponse(id, (byte) 0);
    }

    @Transactional(readOnly = true)
    public KhachHangResponse layChiTiet(Long id) {
        KhachHang khachHang = khachHangRepository.findWithNguoiDungByNguoiDungId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CUSTOMER_NOT_FOUND",
                        "Không tìm thấy khách hàng."
                ));
        return toResponse(khachHang);
    }

    private KhachHangResponse toResponse(KhachHang khachHang) {
        var nguoiDung = khachHang.getNguoiDung();
        return new KhachHangResponse(
                nguoiDung.getId(),
                nguoiDung.getTaiKhoanEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getNgaySinh(),
                nguoiDung.getGioiTinh(),
                nguoiDung.getDiaChi(),
                khachHang.getCmndPassport(),
                khachHang.getQuocTich(),
                khachHang.getXepHang(),
                khachHang.getDiemTichLuy(),
                nguoiDung.getTrangThai()
        );
    }

    private KhachHang timKhachHangDangHoatDong(Long id) {
        KhachHang khachHang = khachHangRepository.findWithNguoiDungByNguoiDungId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CUSTOMER_NOT_FOUND",
                        "Không tìm thấy khách hàng."
                ));
        if (khachHang.getNguoiDung().getTrangThai() == null
                || khachHang.getNguoiDung().getTrangThai() != 1) {
            throw khachHangDaNgungHoatDong();
        }
        return khachHang;
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String encodeOptionalPassword(String password) {
        if (password == null) {
            return null;
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_LONG",
                    "Mật khẩu không được vượt quá 72 byte khi mã hóa.");
        }
        return passwordEncoder.encode(password);
    }

    private BusinessException emailDaTonTai() {
        return new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                "Email đã được đăng ký.");
    }

    private BusinessException khachHangDaNgungHoatDong() {
        return new BusinessException(HttpStatus.CONFLICT, "CUSTOMER_ALREADY_INACTIVE",
                "Khách hàng đã ngừng hoạt động.");
    }
}
