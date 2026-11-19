package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.entity.KhachHang;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.XacThucToken;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.exception.ResourceNotFoundException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import com.team6.quanlyvemaybay.repository.XacThucTokenRepository;
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
    private final PhienLamMoiService phienLamMoiService;
    private final XacThucTokenRepository xacThucTokenRepository;

    public AuthService(
            NguoiDungRepository nguoiDungRepository,
            KhachHangRepository khachHangRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            PhienLamMoiService phienLamMoiService,
            XacThucTokenRepository xacThucTokenRepository
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.phienLamMoiService = phienLamMoiService;
        this.xacThucTokenRepository = xacThucTokenRepository;
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
        khachHang.setCmndPassport(request.cmndPassport() != null ? request.cmndPassport().trim() : null);
        khachHang.setQuocTich(
                request.quocTich() == null || request.quocTich().isBlank()
                        ? "Việt Nam"
                        : request.quocTich().trim()
        );
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
        NguoiDung nguoiDung = findActiveUser(userId);
        return toPersonalInfoResponse(nguoiDung, findCustomerProfile(nguoiDung));
    }

    @Transactional
    public PersonalInfoResponse updatePersonalInfo(Long userId, UpdatePersonalInfoRequest request) {
        NguoiDung nguoiDung = findActiveUser(userId);
        KhachHang khachHang = findCustomerProfile(nguoiDung);
        if (khachHang != null) {
            if (request.cmndPassport() != null) {
                String cmndPassport = request.cmndPassport().trim();
                khachHang.setCmndPassport(cmndPassport.isEmpty() ? null : cmndPassport);
            }
            if (request.quocTich() != null) {
                khachHang.setQuocTich(request.quocTich().trim());
            }
        }
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(request.soDienThoai());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setDiaChi(request.diaChi() != null ? request.diaChi().trim() : null);
        return toPersonalInfoResponse(nguoiDungRepository.save(nguoiDung), khachHang);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        validateBcryptPasswordLength(request.currentPassword());
        validateBcryptPasswordLength(request.newPassword());
        NguoiDung nguoiDung = nguoiDungRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng."));
        if (!Byte.valueOf((byte) 1).equals(nguoiDung.getTrangThai())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
                    "Tài khoản đã bị khóa hoặc chưa được kích hoạt.");
        }
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
        xacThucTokenRepository.thuHoiTheoNguoiDung(userId, XacThucToken.Loai.QUEN_MAT_KHAU);
        phienLamMoiService.thuHoiNguoiDung(userId);
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

    private KhachHang findCustomerProfile(NguoiDung nguoiDung) {
        if (nguoiDung.getVaiTro() != VaiTro.KHACH_HANG) {
            return null;
        }
        return khachHangRepository.findById(nguoiDung.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CUSTOMER_PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ khách hàng."));
    }

    private PersonalInfoResponse toPersonalInfoResponse(NguoiDung nguoiDung, KhachHang khachHang) {
        return new PersonalInfoResponse(
                nguoiDung.getId(),
                nguoiDung.getTaiKhoanEmail(),
                nguoiDung.getHoTen(),
                nguoiDung.getSoDienThoai(),
                nguoiDung.getNgaySinh(),
                nguoiDung.getGioiTinh(),
                nguoiDung.getDiaChi(),
                nguoiDung.getAnhDaiDien(),
                nguoiDung.getVaiTro(),
                khachHang != null ? khachHang.getCmndPassport() : null,
                khachHang != null ? khachHang.getQuocTich() : null
        );
    }

    public record LoginResult(AuthUserResponse user, String accessToken) {
    }
}
