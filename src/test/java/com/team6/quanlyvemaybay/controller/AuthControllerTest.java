package com.team6.quanlyvemaybay.controller;

import com.team6.quanlyvemaybay.dto.auth.AuthUserResponse;
import com.team6.quanlyvemaybay.dto.auth.AvatarUpdateResponse;
import com.team6.quanlyvemaybay.dto.auth.AvatarUploadSignatureResponse;
import com.team6.quanlyvemaybay.dto.auth.ChangePasswordRequest;
import com.team6.quanlyvemaybay.dto.auth.LoginRequest;
import com.team6.quanlyvemaybay.dto.auth.PersonalInfoResponse;
import com.team6.quanlyvemaybay.dto.auth.RegisterRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdatePersonalInfoRequest;
import com.team6.quanlyvemaybay.dto.auth.UpdateAvatarRequest;
import com.team6.quanlyvemaybay.entity.enums.GioiTinh;
import com.team6.quanlyvemaybay.entity.enums.VaiTro;
import com.team6.quanlyvemaybay.service.AuthService;
import com.team6.quanlyvemaybay.service.CloudinaryAvatarService;
import com.team6.quanlyvemaybay.service.PhienLamMoiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;

import java.time.LocalDate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private AuthService authService;
    private CloudinaryAvatarService cloudinaryAvatarService;
    private PhienLamMoiService phienLamMoiService;
    private AuthController controller;
    private final Instant now = Instant.parse("2026-11-17T00:00:00Z");

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        cloudinaryAvatarService = mock(CloudinaryAvatarService.class);
        phienLamMoiService = mock(PhienLamMoiService.class);
        controller = new AuthController(
                authService, cloudinaryAvatarService, phienLamMoiService, false, Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void csrfEndpointMaterializesTokenAndReturnsNoContent() {
        CsrfToken csrfToken = mock(CsrfToken.class);

        var response = controller.csrf(csrfToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(csrfToken).getToken();
    }

    @Test
    void registerReturnsCreatedUser() {
        RegisterRequest request = new RegisterRequest(
                "user@example.com", "password-123", "Nguyễn Văn A", null, null, null,
                "012345678901", "Việt Nam"
        );
        AuthUserResponse user = new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG);
        when(authService.register(request)).thenReturn(user);

        var response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    void loginSetsHttpOnlyAccessCookieAndDoesNotReturnTokenInBody() {
        LoginRequest request = new LoginRequest("user@example.com", "password-123", true);
        AuthUserResponse user = new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG);
        when(authService.login(request)).thenReturn(new AuthService.LoginResult(user, "signed-token"));
        when(phienLamMoiService.tao(42L, true))
                .thenReturn(new PhienLamMoiService.KetQua("refresh-token", null, now.plusSeconds(604800), true));

        var response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody().getData());
        String cookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.contains("access_token=signed-token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertTrue(cookie.contains("Max-Age=900"));
        assertTrue(response.getHeaders().get(HttpHeaders.SET_COOKIE).get(1).contains("Max-Age=604800"));
        assertFalse(response.getBody().toString().contains("signed-token"));
    }

    @Test
    void loginWithoutRememberUsesSessionCookies() {
        LoginRequest request = new LoginRequest("user@example.com", "password-123");
        AuthUserResponse user = new AuthUserResponse(42L, "user@example.com", "Nguyễn Văn A", VaiTro.KHACH_HANG);
        when(authService.login(request)).thenReturn(new AuthService.LoginResult(user, "signed-token"));
        when(phienLamMoiService.tao(42L, false))
                .thenReturn(new PhienLamMoiService.KetQua("refresh-token", null, now.plusSeconds(604800), false));

        var cookies = controller.login(request).getHeaders().get(HttpHeaders.SET_COOKIE);

        assertEquals(2, cookies.size());
        assertTrue(cookies.stream().allMatch(cookie -> !cookie.contains("Max-Age") && cookie.contains("HttpOnly")));
    }

    @Test
    void refreshKeepsTheOriginalAbsoluteExpiry() {
        when(phienLamMoiService.lamMoi("old-refresh"))
                .thenReturn(new PhienLamMoiService.KetQua("new-refresh", "new-access", now.plusSeconds(60), true));

        var cookies = controller.refresh("old-refresh").getHeaders().get(HttpHeaders.SET_COOKIE);

        assertTrue(cookies.get(0).contains("access_token=new-access"));
        assertTrue(cookies.get(1).contains("refresh_token=new-refresh"));
        assertTrue(cookies.get(1).contains("Max-Age=60"));
    }

    @Test
    void meReturnsPersonalInfoForAuthenticatedUser() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        PersonalInfoResponse profile = new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn A", "+84901234567",
                LocalDate.of(1995, 4, 20), GioiTinh.Nam, "Đà Nẵng", null, VaiTro.KHACH_HANG,
                "123456789", "Việt Nam"
        );
        when(authService.currentUser(42L)).thenReturn(profile);

        var response = controller.me(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profile, response.getBody().getData());
    }

    @Test
    void updatePersonalInfoReturnsUpdatedProfile() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        UpdatePersonalInfoRequest request = new UpdatePersonalInfoRequest(
                "Nguyễn Văn B", null, null, GioiTinh.Khac, null, "123456789", "Việt Nam"
        );
        PersonalInfoResponse profile = new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn B", null,
                null, GioiTinh.Khac, null, null, VaiTro.KHACH_HANG, "123456789", "Việt Nam"
        );
        when(authService.updatePersonalInfo(42L, request)).thenReturn(profile);

        var response = controller.updatePersonalInfo(jwt, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profile, response.getBody().getData());
    }

    @Test
    void avatarUploadSignatureIsScopedToAuthenticatedUser() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        AvatarUploadSignatureResponse signature = new AvatarUploadSignatureResponse(
                "cloud", "api-key", "https://api.cloudinary.com/v1_1/cloud/image/upload",
                123L, "avatar_42_0123456789abcdef0123456789abcdef",
                "signed-value", "avatar-preset", "jpg,png,webp", 5 * 1024 * 1024
        );
        when(cloudinaryAvatarService.createUploadSignature(42L)).thenReturn(signature);

        var response = controller.createAvatarUploadSignature(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(signature, response.getBody().getData());
    }

    @Test
    void updateAvatarReturnsProfileAndCleanupStatus() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        UpdateAvatarRequest request = new UpdateAvatarRequest("avatar_42_0123456789abcdef0123456789abcdef");
        PersonalInfoResponse profile = new PersonalInfoResponse(
                42L, "user@example.com", "Nguyễn Văn A", null, null, GioiTinh.Khac,
                null, "https://res.cloudinary.com/cloud/image/upload/avatar.jpg",
                VaiTro.KHACH_HANG, null, "Việt Nam"
        );
        AvatarUpdateResponse result = new AvatarUpdateResponse(
                profile, AvatarUpdateResponse.OldAvatarCleanupStatus.DELETED
        );
        when(cloudinaryAvatarService.updateAvatar(42L, request.publicId())).thenReturn(result);

        var response = controller.updateAvatar(jwt, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody().getData());
    }

    @Test
    void changePasswordReturnsSuccessMessage() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("42")
                .build();
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");

        var response = controller.changePassword(jwt, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Đổi mật khẩu thành công", response.getBody().getMessage());
        verify(authService).changePassword(42L, request);
    }
}
