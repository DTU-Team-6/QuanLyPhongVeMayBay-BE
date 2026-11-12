package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.entity.NguoiDung;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.exception.BusinessException;
import com.team6.quanlyvemaybay.repository.KhachHangRepository;
import com.team6.quanlyvemaybay.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;
    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                nguoiDungRepository,
                khachHangRepository,
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndCreatesCustomerProfile() {
        RegisterRequest request = new RegisterRequest(
                "  User@Example.com ",
                "password-123",
                " Nguyễn Văn A ",
                null,
                null,
                null
        );
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password-123")).thenReturn("bcrypt-hash");
        when(nguoiDungRepository.saveAndFlush(any(NguoiDung.class))).thenAnswer(invocation -> {
            NguoiDung saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        var result = authService.register(request);

        ArgumentCaptor<NguoiDung> userCaptor = ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).saveAndFlush(userCaptor.capture());
        verify(khachHangRepository).save(any());
        NguoiDung saved = userCaptor.getValue();
        assertEquals("user@example.com", saved.getTaiKhoanEmail());
        assertEquals("bcrypt-hash", saved.getMatKhau());
        assertEquals("Nguyễn Văn A", saved.getHoTen());
        assertEquals(GioiTinh.Khac, saved.getGioiTinh());
        assertEquals(VaiTro.KHACH_HANG, saved.getVaiTro());
        assertEquals(Byte.valueOf((byte) 1), saved.getTrangThai());
        assertEquals(42L, result.id());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(nguoiDungRepository.existsByTaiKhoanEmailIgnoreCase("user@example.com")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(new RegisterRequest(
                        "User@example.com", "password-123", "Nguyễn Văn A", null, null, null
                ))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("EMAIL_ALREADY_REGISTERED", exception.getCode());
        verify(nguoiDungRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerRejectsPasswordLongerThanBcryptByteLimit() {
        String password = "😀".repeat(20);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(new RegisterRequest(
                        "User@example.com", password, "Nguyễn Văn A", null, null, null
                ))
        );

        assertEquals("PASSWORD_TOO_LONG", exception.getCode());
        verify(passwordEncoder, never()).encode(any());
        verify(nguoiDungRepository, never()).existsByTaiKhoanEmailIgnoreCase(any());
    }

    @Test
    void loginRejectsPasswordLongerThanBcryptByteLimit() {
        String password = "😀".repeat(20);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("user@example.com", password))
        );

        assertEquals("PASSWORD_TOO_LONG", exception.getCode());
        verify(nguoiDungRepository, never()).findByTaiKhoanEmailIgnoreCase(any());
    }

    @Test
    void loginReturnsUserAndAccessTokenWhenCredentialsAreValid() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findByTaiKhoanEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password-123", "bcrypt-hash")).thenReturn(true);
        when(jwtTokenService.createAccessToken(user)).thenReturn("signed-access-token");

        AuthService.LoginResult result = authService.login(new LoginRequest(" User@Example.com ", "password-123"));

        assertEquals("user@example.com", result.user().email());
        assertEquals("signed-access-token", result.accessToken());
    }

    @Test
    void loginUsesGenericErrorWhenPasswordDoesNotMatch() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findByTaiKhoanEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("user@example.com", "wrong-password"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("INVALID_CREDENTIALS", exception.getCode());
    }

    @Test
    void updatePersonalInfoChangesOnlyEditableProfileFields() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(nguoiDungRepository.save(user)).thenReturn(user);

        var result = authService.updatePersonalInfo(42L, new UpdatePersonalInfoRequest(
                "  Nguyễn Văn B  ",
                "+84901234567",
                java.time.LocalDate.of(1995, 4, 20),
                GioiTinh.Nam,
                "  Đà Nẵng  "
        ));

        assertEquals("Nguyễn Văn B", result.hoTen());
        assertEquals("user@example.com", result.email());
        assertEquals("+84901234567", result.soDienThoai());
        assertEquals("Đà Nẵng", result.diaChi());
        assertEquals(VaiTro.KHACH_HANG, result.vaiTro());
        assertEquals("bcrypt-hash", user.getMatKhau());
        verify(nguoiDungRepository).save(user);
    }

    @Test
    void changePasswordVerifiesCurrentPasswordAndStoresNewHash() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "bcrypt-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "bcrypt-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-bcrypt-hash");

        authService.changePassword(42L, new ChangePasswordRequest("old-password", "new-password"));

        assertEquals("new-bcrypt-hash", user.getMatKhau());
        verify(nguoiDungRepository).save(user);
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.changePassword(
                        42L,
                        new ChangePasswordRequest("wrong-password", "new-password")
                )
        );

        assertEquals("CURRENT_PASSWORD_INVALID", exception.getCode());
        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    void changePasswordRejectsSamePassword() {
        NguoiDung user = activeUser();
        when(nguoiDungRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "bcrypt-hash")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.changePassword(
                        42L,
                        new ChangePasswordRequest("old-password", "old-password")
                )
        );

        assertEquals("PASSWORD_UNCHANGED", exception.getCode());
        verify(nguoiDungRepository, never()).save(any());
    }

    private NguoiDung activeUser() {
        NguoiDung user = new NguoiDung();
        user.setId(42L);
        user.setTaiKhoanEmail("user@example.com");
        user.setMatKhau("bcrypt-hash");
        user.setHoTen("Nguyễn Văn A");
        user.setVaiTro(VaiTro.KHACH_HANG);
        user.setTrangThai((byte) 1);
        return user;
    }
}
