package com.team6.quanlyvemaybay.service;

import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
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
