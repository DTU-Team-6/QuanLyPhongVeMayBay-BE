package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            NguoiDungRepository nguoiDungRepository,
            KhachHangRepository khachHangRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        validateBcryptPasswordLength(request.password());
        String email = normalizeEmail(request.email());
        if (nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase(email)) {
            throw emailDaTonTai();
        }

        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setTaiKhoanEmail(email);
        nguoiDung.setMatKhau(passwordEncoder.encode(request.password()));
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(request.soDienThoai());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh() != null ? request.gioiTinh() : GioiTinh.Khac);
        nguoiDung.setVaiTro(VaiTro.KHACH_HANG);
        nguoiDung.setTrangThai((byte) 1);

        try {
            nguoiDung = nguoiDungRepository.saveAndFlush(nguoiDung);
        } catch (DataIntegrityViolationException ex) {
            throw emailDaTonTai();
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setNguoiDung(nguoiDung);
        khachHangRepository.save(khachHang);
        return toResponse(nguoiDung);
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        validateBcryptPasswordLength(request.password());
        String email = normalizeEmail(request.email());
        NguoiDung nguoiDung = nguoiDungRepository.findByTaiKhoanEmailIgnoreCase(email)
                .orElseThrow(this::saiThongTinDangNhap);

        String passwordHash = nguoiDung.getMatKhau();
        if (passwordHash == null || !passwordEncoder.matches(request.password(), passwordHash)) {
            throw saiThongTinDangNhap();
        }
        if (nguoiDung.getTrangThai() == null || nguoiDung.getTrangThai() != 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
                    "Tài khoản đã bị khóa hoặc chưa được kích hoạt.");
        }

        return new LoginResult(toResponse(nguoiDung), jwtTokenService.createAccessToken(nguoiDung));
    }

    @Transactional(readOnly = true)
    public PersonalInfoResponse currentUser(Long userId) {
        return toPersonalInfoResponse(findActiveUser(userId));
    }

    @Transactional
    public PersonalInfoResponse updatePersonalInfo(Long userId, UpdatePersonalInfoRequest request) {
        NguoiDung nguoiDung = findActiveUser(userId);
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(request.soDienThoai());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setDiaChi(request.diaChi() != null ? request.diaChi().trim() : null);
        return toPersonalInfoResponse(nguoiDungRepository.save(nguoiDung));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        validateBcryptPasswordLength(request.currentPassword());
        validateBcryptPasswordLength(request.newPassword());
        NguoiDung nguoiDung = findActiveUser(userId);
        String passwordHash = nguoiDung.getMatKhau();
        if (passwordHash == null || !passwordEncoder.matches(request.currentPassword(), passwordHash)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CURRENT_PASSWORD_INVALID",
                    "Mật khẩu hiện tại không đúng.");
        }
        if (passwordEncoder.matches(request.newPassword(), passwordHash)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_UNCHANGED",
                    "Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        nguoiDung.setMatKhau(passwordEncoder.encode(request.newPassword()));
        nguoiDungRepository.save(nguoiDung);
    }

    private NguoiDung findActiveUser(Long userId) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng."));
        if (nguoiDung.getTrangThai() == null || nguoiDung.getTrangThai() != 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
                    "Tài khoản đã bị khóa hoặc chưa được kích hoạt.");
        }
        return nguoiDung;
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private void validateBcryptPasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_LONG",
                    "Mật khẩu không được vượt quá 72 byte khi mã hóa.");
        }
    }

    private BusinessException emailDaTonTai() {
        return new BusinessException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                "Email đã được đăng ký.");
    }

    private BusinessException saiThongTinDangNhap() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Email hoặc mật khẩu không đúng.");
    }

    private AuthUserResponse toResponse(NguoiDung nguoiDung) {
        return new AuthUserResponse(
                nguoiDung.getId(),
                nguoiDung.getTaiKhoanEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getVaiTro()
        );
    }

    private PersonalInfoResponse toPersonalInfoResponse(NguoiDung nguoiDung) {
        return new PersonalInfoResponse(
                nguoiDung.getId(),
                nguoiDung.getTaiKhoanEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getNgaySinh(),
                nguoiDung.getGioiTinh(),
                nguoiDung.getDiaChi(),
                nguoiDung.getAnhDaiDien(),
                nguoiDung.getVaiTro()
        );
    }

    public record LoginResult(AuthUserResponse user, String accessToken) {
    }
}
